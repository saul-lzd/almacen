const TOKEN_KEY = "almacen.token";

interface JwtPayload {
    sub: string;
    idUsuario: number;
    rol: string;
    nombre: string;
    exp: number;
}

function decodePayload(): JwtPayload | null {
    const token = getToken();
    if (!token) return null;
    try {
        const base64 = token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/");
        // atob() devuelve una "binary string" (1 char = 1 byte) — hay que
        // reinterpretar esos bytes como UTF-8 explícitamente, si no, cualquier
        // acento en el payload (ej. "nombre":"Almacén") sale como mojibake.
        const bytes = Uint8Array.from(atob(base64), c => c.charCodeAt(0));
        const json = new TextDecoder("utf-8").decode(bytes);
        return JSON.parse(json) as JwtPayload;
    } catch {
        return null;
    }
}

export function getToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
}

export function setSession(token: string): void {
    sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearSession(): void {
    sessionStorage.removeItem(TOKEN_KEY);
}

export function isAuthenticated(): boolean {
    const payload = decodePayload();
    if (!payload) return false;
    return payload.exp * 1000 > Date.now();
}

export function getRole(): string | null {
    return decodePayload()?.rol ?? null;
}

export function getNombre(): string | null {
    return decodePayload()?.nombre ?? null;
}

export function getIdUsuario(): number | null {
    return decodePayload()?.idUsuario ?? null;
}

export function redirectToLogin(): void {
    clearSession();
    window.location.href = "login.html";
}
