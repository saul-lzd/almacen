// ================================================================
// API SERVICE — punto central de llamadas al backend
// ================================================================

import { getToken, redirectToLogin } from "./auth";

export const BASE_URL = "http://localhost:8080";

// ── Helpers internos ─────────────────────────────────────────────

function authHeader(): Record<string, string> {
    const token = getToken();
    return token ? { "Authorization": `Bearer ${token}` } : {};
}

async function handleResponse<T>(res: Response): Promise<T> {
    if (res.status === 401) {
        redirectToLogin();
        throw new Error("Sesión expirada");
    }
    if (!res.ok) {
        const err = await res.json().catch(() => null);
        throw new Error(err?.errores?.[0] ?? err?.mensaje ?? `Error ${res.status}`);
    }
    return res.json() as Promise<T>;
}

function jsonInit(method: string, body?: unknown): RequestInit {
    return {
        method,
        headers: { "Content-Type": "application/json", ...authHeader() },
        ...(body !== undefined && { body: JSON.stringify(body) }),
    };
}

function getInit(): RequestInit {
    return { headers: authHeader() };
}

// ================================================================
// CONTRATOS
// ================================================================

export const contratosApi = {

    listar: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/contratos`, getInit()).then(r => handleResponse(r)),

    obtener: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}`, getInit()).then(r => handleResponse(r)),

    crear: (payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos`, jsonInit("POST", payload)).then(r => handleResponse(r)),

    actualizar: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}`, jsonInit("PUT", payload)).then(r => handleResponse(r)),

    actualizarFechaTentativa: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/fecha-tentativa`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),

    enviarAlmacen: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/enviar-almacen`, jsonInit("PATCH")).then(r => handleResponse(r)),

    autorizarEntrega: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/autorizar-entrega`, jsonInit("PATCH")).then(r => handleResponse(r)),

    registrarRecepcion: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/recepcion`, jsonInit("POST", payload)).then(r => handleResponse(r)),

    obtenerBienesAlmacen: (id: number): Promise<any[]> =>
        fetch(`${BASE_URL}/api/contratos/${id}/almacen-bienes`, getInit()).then(r => handleResponse(r)),

    obtenerBienesEntrega: (id: number): Promise<any[]> =>
        fetch(`${BASE_URL}/api/contratos/${id}/bienes-entrega`, getInit()).then(r => handleResponse(r)),

    registrarEntrega: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/entrega`, jsonInit("POST", payload)).then(r => handleResponse(r)),
};

// ================================================================
// ALMACÉN BIENES
// ================================================================

export const almacenBienesApi = {

    guardarDatos: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/almacen-bienes/${id}/datos`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),

    procesarUnidad: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/almacen-bienes/${id}/procesar`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),

    procesarBloque: (payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/almacen-bienes/procesar-bloque`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),
};

// ================================================================
// CATÁLOGOS
// ================================================================

export const catalogosApi = {

    clavePresupuestales: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/claves-presupuestales`, getInit()).then(r => handleResponse(r)),

    unidadesMedida: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/unidadesMedida`, getInit()).then(r => handleResponse(r)),

    funcionarios: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/funcionarios`, getInit()).then(r => handleResponse(r)),
};
