import * as Composite from "ojs/ojcomposite";
import * as view from "text!./quill-editor.html";
import * as metadata from "text!./component.json";

class QuillEditorViewModel {

    private quillInstance: any = null;
    private editorId: string;
    private context: any;
    private initialized: boolean = false;
    private debounceTimer: any = null;
    private pendingValue: string | null = null;  // valor recibido antes de que Quill esté listo

    constructor(context: any) {
        this.context = context;
        this.editorId = "quill-editor-" + Math.random().toString(36).substr(2, 9);
    }

    connected(): void {
        this.initQuill();
    }

    disconnected(): void {
        this.quillInstance = null;
        this.initialized = false; // permite reinicializar si Oracle JET reconecta la instancia
    }

    // Oracle JET invoca este método cuando el padre actualiza una propiedad
    propertyChanged(context: { property: string; value: any }): void {
        if (context.property !== "value") return;

        if (this.quillInstance) {
            this.applyValue(context.value);
        } else {
            // Quill aún no está listo — guardar para aplicar al finalizar initQuill
            this.pendingValue = context.value ?? "";
        }
    }

    // Aplica un valor HTML al editor sin disparar text-change (fuente 'silent')
    private applyValue(value: string): void {
        const currentHTML = this.quillInstance.getSemanticHTML();
        if (value === currentHTML) return;

        if (!value || value === "") {
            this.quillInstance.setContents([], "silent");
        } else {
            const delta = this.quillInstance.clipboard.convert({ html: value });
            this.quillInstance.setContents(delta, "silent");
        }
    }

    private initQuill(): void {
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

        // Limpiar cualquier estructura de Quill previa antes de reinicializar:
        // Quill inserta el toolbar como sibling anterior al contenedor — lo removemos
        const parent = editorEl.parentElement;
        if (parent) {
            parent.querySelectorAll(".ql-toolbar").forEach(el => el.remove());
        }
        editorEl.innerHTML = "";
        editorEl.className = "";

        this.initialized = true;

        const props = this.context.properties;
        // !!props.readonly cubre tanto boolean true como string "true" (HTML attribute coercion)
        const isReadonly = !!props.readonly;

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

        // Aplicar valor pendiente (llegó antes de que Quill estuviera listo) o el inicial
        const initialValue = this.pendingValue !== null ? this.pendingValue : props.value;
        this.pendingValue = null;
        if (initialValue) {
            const delta = this.quillInstance.clipboard.convert({ html: initialValue });
            this.quillInstance.setContents(delta, "silent");
        }

        const quill = this.quillInstance;
        quill.on("text-change", (_delta: any, _old: any, source: string) => {
            // Ignorar cambios programáticos ('silent', 'api') — solo reaccionar al usuario
            if (source !== "user") return;

            clearTimeout(this.debounceTimer);
            this.debounceTimer = setTimeout(() => {
                const html = quill.getSemanticHTML();
                this.context.properties.value = html;
                this.context.element.dispatchEvent(
                    new CustomEvent("valueChanged", {
                        bubbles: true,
                        detail: { value: html }
                    })
                );
            }, 300);
        });
    }
}

Composite.register("quill-editor", {
    view: view,
    viewModel: QuillEditorViewModel,
    metadata: JSON.parse(metadata)
});

export = QuillEditorViewModel;
