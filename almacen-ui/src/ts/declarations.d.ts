// Permite importar archivos HTML y JSON como strings en módulos AMD
declare module "text!*" {
    const content: string;
    export = content;
}