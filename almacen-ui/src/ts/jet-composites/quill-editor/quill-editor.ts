import * as Composite from "ojs/ojcomposite";
import * as view from "text!./quill-editor.html";
import * as metadata from "text!./component.json";

class QuillEditorViewModel {

    private quillInstance: any = null;
    private editorId: string;
    private context: any;
    private initialized: boolean = false;

    constructor(context: any) {
        console.log("QuillEditorViewModel constructor", context);
        this.context = context;
        this.editorId = "quill-editor-" + Math.random().toString(36).substr(2, 9);
    }

    connected(): void {
        this.initQuill();
    }

    disconnected(): void {
        this.quillInstance = null;
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
        const placeholder = props.placeholder || "Escribe aquí...";
        const initialValue = props.value || "";

        this.quillInstance = new Quill(`#${this.editorId}`, {
            theme: "snow",
            readOnly: isReadonly,
            placeholder: placeholder,
            modules: {
                toolbar: isReadonly ? false : [
                    [{ header: [2, 3, false] }],
                    ["bold", "italic", "underline"],
                    [{ list: "ordered" }, { list: "bullet" }],
                    ["clean"]
                ]
            }
        });

        
        if (initialValue) {
            this.quillInstance.clipboard.dangerouslyPasteHTML(initialValue);
        }

        const quill = this.quillInstance;
        // Notificar al padre cuando cambia el contenido
        quill.on("text-change", () => {
            const html = quill.getSemanticHTML();
            // Disparar evento custom para que el padre actualice su observable
            this.context.element.dispatchEvent(
                new CustomEvent("valueChanged", {
                    bubbles: true,
                    detail: { value: html }
                })
            );
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