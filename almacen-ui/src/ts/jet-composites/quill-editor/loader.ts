// El optimizador de "ojet build --release" espera un módulo <componente>/loader.js
// por convención para cada composite local. quill-editor.ts ya se auto-registra
// (Composite.register) al ser importado, así que este loader solo dispara esa carga.
import "./quill-editor";
