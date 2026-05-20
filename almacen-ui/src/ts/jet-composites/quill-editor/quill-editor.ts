import * as Composite from "ojs/ojcomposite";
import * as view from "text!./quill-editor.html";
import * as metadata from "text!./component.json";

class QuillEditorViewModel {

    private quillInstance: any = null;
    private editorId: string;
    private context: any;
    private initialized: boolean = false;
    private debounceTimer: any = null;

    constructor(context: any) {
        this.context = context;
        this.editorId = "quill-editor-" + Math.random().toString(36).substr(2, 9);
    }

    connected(): void {
        this.initQuill();
    }

    disconnected(): void {
        this.quillInstance = null;
    }

    // Oracle JET llama este método cuando una propiedad cambia desde el padre
    propertyChanged(context: { property: string; value: any }): void {
        if (context.property === "value" && this.quillInstance) {
            const currentHTML = this.quillInstance.getSemanticHTML();
            // Solo actualizar si el valor externo es diferente al contenido actual
            // para evitar loop infinito (editor cambia → padre actualiza → editor cambia...)
            if (context.value !== currentHTML) {
                if (!context.value || context.value === "") {
                    this.quillInstance.setContents([]);
                } else {
                    this.quillInstance.clipboard.dangerouslyPasteHTML(context.value);
                }
            }
        }
    }

    private initQuill(): void {

        // Si ya se inicializó, no hacer nada
        if (this.initialized) return;

        const editorEl = document.getElementById(this.editorId);
        if (!editorEl) {
            setTimeout(() => this.initQuill(), 100);
            return;
        }

        const Quill = (window as any).Quill;
        if (!Quill) {
            setTimeout(() => this.initQuill(), 100);
            return;
        }

        // Marcar como inicializado ANTES de crear la instancia
        this.initialized = true;

        // En CCA las propiedades son valores planos — NO funciones/observables
        const props = this.context.properties;
        const isReadonly = props.readonly === true;
        //const placeholder = props.placeholder || "Escribe aquí...";
        //const initialValue = props.value || "";

        this.quillInstance = new Quill(`#${this.editorId}`, {
            theme: "snow",
            readOnly: isReadonly,
            placeholder: props.placeholder,
            modules: {
                toolbar: isReadonly ? false : [
                    [{ header: [2, 3, false] }],
                    ["bold", "italic", "underline"],
                    [{ list: "ordered" }, { list: "bullet" }],
                    ["clean"]
                ]
            }
        });

         // Cargar valor inicial antes del listener
        if (props.value) {
            this.quillInstance.clipboard.dangerouslyPasteHTML(props.value);
        }

        const quill = this.quillInstance;
        // Notificar al padre cuando cambia el contenido
        quill.on("text-change", () => {
            clearTimeout(this.debounceTimer);
            this.debounceTimer = setTimeout(() => {
                const html = quill.getSemanticHTML();
                // Disparar evento custom para que el padre actualice su observable
                this.context.properties.value = html;
                this.context.element.dispatchEvent(
                    new CustomEvent("valueChanged", {
                        bubbles: true,
                        detail: { value: html }
                    })
                );
            }, 300); // Debounce de 300ms para evitar actualizaciones excesivas
        });
    }

    public reset(): void {
        if (this.quillInstance) {
            this.quillInstance.setContents([]);
        }
    }
}

Composite.register("quill-editor", {
    view: view,
    viewModel: QuillEditorViewModel,
    metadata: JSON.parse(metadata)
});

export = QuillEditorViewModel;