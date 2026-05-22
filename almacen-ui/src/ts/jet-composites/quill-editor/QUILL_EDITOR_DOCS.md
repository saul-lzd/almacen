# Documentación: componente quill-editor

"¿Cómo funciona todo el módulo de quill-editor? 
 ¿Cómo se conecta el componente con los observables y cómo se lanzan los eventos?"

---

## 1. El problema que resuelve

`frmBienDescripcionHtml` es un observable de KnockoutJS en el ViewModel del
contrato. El problema es que Quill es una librería externa que no sabe nada de
KO. El componente CCA actúa como el puente entre los dos mundos.

```
ViewModel (KO)           componente CCA           Quill (librería)
─────────────────        ──────────────────       ─────────────────
frmBienDescripcionHtml   ←→  value property   ←→  quillInstance
```

---

## 2. Archivos del componente

```
jet-composites/
└── quill-editor/
    ├── quill-editor.ts      ← lógica (ViewModel)
    ├── quill-editor.html    ← template visual
    └── component.json       ← contrato/metadatos del componente
```

---

## 3. component.json — el contrato público

Es la declaración formal de qué acepta y qué produce el componente.
Oracle JET lo lee para saber cómo conectar el componente con el mundo exterior.

```json
{
  "name": "quill-editor",
  "version": "1.0.0",
  "jetVersion": "^17.0.0",
  "properties": {
    "value": {
      "type": "string",
      "writeback": true
    },
    "placeholder": {
      "type": "string"
    },
    "readonly": {
      "type": "boolean",
      "value": false
    }
  },
  "events": {
    "valueChanged": {
      "bubbles": true
    }
  }
}
```

### properties
Cada entrada define un atributo que el padre puede pasarle al componente en el HTML.

- "type": qué tipo espera Oracle JET ("string", "boolean", "number", etc.)
- "value": valor por defecto si el padre no lo pasa
- "writeback": true — permite que el componente actualice de vuelta el observable
  del padre. Habilita el binding {{}} bidireccional. Sin esto, solo funciona [[]]
  (una sola dirección: padre → componente).

### events
Cada entrada declara un evento custom que el componente puede emitir.
Si está declarado aquí, el padre puede escucharlo con on-nombre-evento="[[handler]]".
"bubbles": true hace que el evento suba por el árbol DOM.

IMPORTANTE: si una propiedad o evento no está declarado en component.json,
Oracle JET lo ignora — el binding no funcionará aunque el código TS lo emita.

---

## 4. quill-editor.html — el template

```html
<div :id="[[editorId]]"></div>
```

Es un único div vacío. Quill toma ese div y lo transforma al inicializarse:
inserta el toolbar como elemento hermano justo antes del div, y convierte el
div en el contenedor del editor. El :id usa el editorId generado en el
constructor para que cada instancia tenga un elemento único en el DOM.

---

## 5. quill-editor.ts — explicación línea por línea

### Imports

```typescript
import * as Composite from "ojs/ojcomposite";
import * as view from "text!./quill-editor.html";
import * as metadata from "text!./component.json";
```

- Composite: módulo de Oracle JET para registrar componentes CCA. Solo se usa
  al final en Composite.register(...).
- view: el HTML del template. El prefijo "text!" es una convención de RequireJS
  que le dice al bundler "carga este archivo como texto plano".
- metadata: el component.json cargado como texto. Se parsea con JSON.parse()
  al registrar el componente.


### Variables privadas

```typescript
private quillInstance: any = null;
```
La instancia de Quill creada en initQuill(). Es null hasta que Quill se
inicializa y vuelve a null cuando el componente se desconecta. Todos los
métodos que interactúan con Quill verifican que no sea null antes de usarla.

```typescript
private editorId: string;
```
ID único generado en el constructor. Necesario porque puede haber múltiples
instancias de quill-editor en la misma página (formulario y dialog). Si ambas
tuvieran el mismo ID, Quill inicializaría en el elemento equivocado.

```typescript
private context: any;
```
El objeto que Oracle JET inyecta al componente. Contiene dos cosas clave:
- context.properties: los valores actuales de las propiedades del component.json
- context.element: el elemento DOM del componente (<quill-editor> en el HTML)

```typescript
private initialized: boolean = false;
```
Flag que evita que initQuill() corra más de una vez. Sin esto, si Oracle JET
llamara connected() dos veces, se crearían dos instancias de Quill sobre el
mismo elemento y aparecerían dos toolbars solapadas.

```typescript
private debounceTimer: any = null;
```
Referencia al timer del debounce. Se usa con clearTimeout() y setTimeout() para
asegurarse de que solo se notifique al padre cuando el usuario termina de
escribir, no en cada tecla individual.

```typescript
private pendingValue: string | null = null;
```
Guarda temporalmente un valor que llegó via propertyChanged() antes de que
Quill estuviera listo. Si el padre actualiza el observable mientras Quill aún
se inicializa, el valor no se pierde — se guarda aquí y se aplica al terminar
initQuill().


### constructor

```typescript
constructor(context: any) {
    this.context = context;
    this.editorId = "quill-editor-" + Math.random().toString(36).substr(2, 9);
}
```

Oracle JET crea una instancia de esta clase por cada <quill-editor> en el HTML
e inyecta context automáticamente. El ID aleatorio garantiza unicidad:
Math.random().toString(36) genera algo como "0.k3p9xz", y .substr(2, 9)
toma 9 caracteres desde la posición 2, resultando en "k3p9xz...".


### connected()

```typescript
connected(): void {
    this.initQuill();
}
```

Oracle JET lo llama cuando el elemento <quill-editor> es insertado en el DOM.
Es el punto de entrada correcto para inicializar librerías externas porque en
este momento ya existe el elemento en la página y se puede buscar por ID.


### disconnected()

```typescript
disconnected(): void {
    this.quillInstance = null;
    this.initialized = false;
}
```

Oracle JET lo llama cuando el elemento es removido del DOM. Se limpia
quillInstance para liberar la referencia (ayuda al garbage collector).
Se resetea initialized para que si Oracle JET reconecta la misma instancia
de la clase, initQuill() pueda volver a correr y crear una instancia fresca.


### propertyChanged()

```typescript
propertyChanged(context: { property: string; value: any }): void {
    if (context.property !== "value") return;

    if (this.quillInstance) {
        this.applyValue(context.value);
    } else {
        this.pendingValue = context.value ?? "";
    }
}
```

Oracle JET lo llama AUTOMÁTICAMENTE cada vez que el padre actualiza una
propiedad del componente. Por ejemplo, cuando el ViewModel hace:

    this.frmBienDescripcionHtml("nuevo texto");

Oracle JET detecta el cambio en el binding value="{{frmBienDescripcionHtml}}"
y llama a este método con:

    { property: "value", value: "nuevo texto" }

El nombre "propertyChanged" es OBLIGATORIO — Oracle JET lo busca por ese
nombre exacto en el ViewModel. Si se escribe diferente, no se llama.

- if (context.property !== "value") return: el componente tiene tres propiedades
  (value, placeholder, readonly). Solo nos interesa reaccionar a cambios en
  "value". Si cambiara placeholder o readonly en tiempo de ejecución, no hay
  nada que hacer.
- Si Quill ya existe → aplicar el valor inmediatamente.
- Si Quill aún no existe → guardar en pendingValue para aplicarlo después.


### applyValue() — método privado

```typescript
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
```

Actualiza el contenido visual de Quill sin crear efectos secundarios.

- getSemanticHTML(): obtiene el HTML actual del editor. Se compara con el nuevo
  valor para no hacer trabajo innecesario si el contenido ya es el correcto.

- setContents([], "silent"): limpia el editor. El array vacío [] es un Delta
  de Quill que representa contenido vacío.

- clipboard.convert({ html: value }): convierte HTML a un Delta. Quill trabaja
  internamente con Deltas (formato propio que describe el contenido como
  operaciones), no con HTML crudo.

- setContents(delta, "silent"): aplica el Delta al editor. La palabra "silent"
  es la clave: le dice a Quill que este cambio es programático, no del usuario.
  Con "silent", Quill NO dispara el evento text-change, lo que evita el ciclo
  de retroalimentación:
    componente actualiza Quill
    → Quill notifica al componente
    → componente actualiza Quill
    → ... (bucle infinito)


### initQuill() — método privado

GUARDAS DE SEGURIDAD:

```typescript
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
```

Tres condiciones deben cumplirse antes de inicializar:
1. No haberse inicializado ya (flag initialized).
2. El elemento DOM debe existir (puede que Oracle JET aún no lo haya renderizado).
3. Quill debe estar cargado en window (se carga via CDN en el HTML principal).

Si alguna falla, se reintenta en 100ms. El cast (window as any) es necesario
porque TypeScript no conoce la variable global Quill — es una forma de decirle
"confía en mí, sé que existe en tiempo de ejecución".

LIMPIEZA PREVIA:

```typescript
const parent = editorEl.parentElement;
if (parent) {
    parent.querySelectorAll(".ql-toolbar").forEach(el => el.remove());
}
editorEl.innerHTML = "";
editorEl.className = "";
```

Cuando Quill inicializa, transforma el div contenedor: inserta su toolbar como
elemento hermano justo antes del div y añade clases CSS. Si initQuill() corre
por segunda vez (Oracle JET desconectó y reconectó el componente), el DOM ya
tiene esa estructura. Sin limpiarla primero, Quill añadiría una segunda toolbar
encima. Esta limpieza previene el bug de toolbars solapadas.

CREACIÓN DE LA INSTANCIA:

```typescript
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
```

"snow" es el tema visual de Quill. Si isReadonly es true, no se monta toolbar
(toolbar: false). El array define los botones disponibles: header es el dropdown
de tamaño de texto, luego negrita/cursiva/subrayado, listas ordenadas y sin
ordenar, y el botón de limpiar formato.

    !!props.readonly se usa en lugar de props.readonly === true porque Oracle JET
    puede pasar el atributo HTML readonly="true" como el string "true" en lugar
    del booleano true. Con !! ambos casos resultan en true.

VALOR INICIAL:

```typescript
const initialValue = this.pendingValue !== null ? this.pendingValue : props.value;
this.pendingValue = null;
if (initialValue) {
    const delta = this.quillInstance.clipboard.convert({ html: initialValue });
    this.quillInstance.setContents(delta, "silent");
}
```

Después de crear Quill se aplica el contenido inicial. La prioridad es
pendingValue (un valor que llegó mientras Quill no estaba listo) sobre
props.value (el valor que tenía la propiedad cuando el componente se montó).
Se usa el mismo mecanismo "silent" para no disparar text-change.

LISTENER DE ESCRITURA DEL USUARIO:

```typescript
const quill = this.quillInstance;
quill.on("text-change", (_delta, _old, source) => {
    if (source !== "user") return;

    clearTimeout(this.debounceTimer);
    this.debounceTimer = setTimeout(() => {
        const html = quill.getSemanticHTML();
        this.context.properties.value = html;
        this.context.element.dispatchEvent(
            new CustomEvent("valueChanged", { bubbles: true, detail: { value: html } })
        );
    }, 300);
});
```

"const quill = this.quillInstance" captura la referencia localmente. Dentro
del callback de text-change, "this" apuntaría al contexto del evento, no a la
clase — la variable local evita ese problema.

source !== "user" filtra los cambios que el propio componente generó con
"silent". Solo se notifica al padre cuando el cambio vino del teclado.

El debounce espera 300ms de silencio antes de notificar. Sin él, cada tecla
dispararía una actualización al padre.

this.context.properties.value = html activa el mecanismo de writeback de
Oracle JET — actualiza frmBienDescripcionHtml en el padre a través del binding
{{}} gracias a "writeback": true en component.json.

dispatchEvent("valueChanged") emite el evento custom hacia arriba por el DOM.
El padre lo captura con on-value-changed="[[onQuillChanged]]". Es redundante
con el writeback pero funciona como respaldo si el writeback fallara.


### Composite.register()

```typescript
Composite.register("quill-editor", {
    view: view,
    viewModel: QuillEditorViewModel,
    metadata: JSON.parse(metadata)
});
```

Este es el momento en que Oracle JET registra el tag HTML <quill-editor>.
A partir de aquí, cada vez que Oracle JET encuentre ese tag en el DOM, creará
una instancia de QuillEditorViewModel, renderizará view como su template, y
usará metadata (el component.json parseado) para saber qué propiedades y
eventos existen.

"export = QuillEditorViewModel" es necesario para que el sistema de módulos de
RequireJS lo encuentre cuando otros archivos lo importen.

---

## 6. Flujo completo: cómo se conecta todo

### Dirección padre → Quill (ejemplo: cargar datos al editar un bien)

```
cmdEditarBien(bien)
  └─ frmBienDescripcionHtml(bien.descripcionTecnica)   [KO observable cambia]
       └─ Oracle JET detecta el cambio en el binding {{}}
            └─ propertyChanged({ property: "value", value: "..." })
                 └─ applyValue("...")
                      └─ quill.setContents(delta, "silent")  [Quill muestra el contenido]
                           └─ text-change NO se dispara (fuente "silent") → no hay bucle
```

### Dirección Quill → padre (ejemplo: usuario escribe en el editor)

```
Usuario escribe en el editor
  └─ Quill dispara text-change (source = "user")
       └─ debounce 300ms
            ├─ context.properties.value = html
            │    └─ writeback Oracle JET → frmBienDescripcionHtml se actualiza
            └─ dispatchEvent("valueChanged")
                 └─ on-value-changed → onQuillChanged → frmBienDescripcionHtml(html)
                      (redundante con el writeback, pero funciona como respaldo)
```

### La condición de carrera (pendingValue)

```
t=0ms    Oracle JET llama connected() → initQuill() → DOM no listo → reintenta en 100ms
t=5ms    propertyChanged() recibe un valor → quillInstance es null → guarda en pendingValue
t=100ms  initQuill() reintenta → DOM listo → crea Quill → lee pendingValue → lo aplica
```

Sin pendingValue, el valor del t=5ms se perdería silenciosamente.

---

## 7. Métodos de ciclo de vida — nombres obligatorios

Los nombres son exactos y obligatorios. Oracle JET los busca por nombre en el
objeto ViewModel después de instanciarlo. Si se escriben diferente, no se llaman.

| Método              | Cuándo lo llama Oracle JET                    | Obligatorio |
|---------------------|-----------------------------------------------|-------------|
| constructor(context)| Al crear la instancia                         | Sí          |
| connected()         | Cuando el elemento entra al DOM               | No*         |
| disconnected()      | Cuando el elemento sale del DOM               | No*         |
| propertyChanged(ctx)| Cuando el padre actualiza una propiedad       | No*         |
| transitionCompleted()| Tras animaciones del padre                   | No*         |

* No obligatorio técnicamente, pero en la práctica casi siempre necesario.

---

## 8. Cómo el padre usa el componente

```html
<quill-editor
    value="{{frmBienDescripcionHtml}}"
    placeholder="Escribe aquí..."
    on-value-changed="[[onQuillChanged]]">
</quill-editor>
```

| Sintaxis           | Significado                                                    |
|--------------------|----------------------------------------------------------------|
| [[expresion]]      | Una dirección: padre → componente (solo lectura)               |
| {{observable}}     | Dos direcciones: padre ↔ componente (requiere writeback: true) |
| on-nombre-evento   | Escucha el evento declarado en "events" del component.json     |
