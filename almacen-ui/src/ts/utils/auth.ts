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
        return JSON.parse(atob(base64)) as JwtPayload;
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
