// ================================================================
// API SERVICE — punto central de llamadas al backend
// Todas las llamadas pasan por aquí para garantizar autenticación
// y manejo uniforme de errores.
// ================================================================

import { getToken, redirectToLogin } from "./auth";

export const BASE_URL = "http://localhost:8080";

// ── Helpers internos ─────────────────────────────────────────────

function authHeader(): Record<string, string> {
    const token = getToken();
    return token ? { "Authorization": `Bearer ${token}` } : {};
}

// Redirige al login si el servidor responde 401.
// Para otros errores, extrae el primer mensaje del cuerpo { errores[], mensaje }.
async function handleResponse<T>(res: Response): Promise<T> {
    if (res.status === 401) {
        redirectToLogin();
        throw new Error("Sesión expirada");
    }
    if (!res.ok) {
        const text = await res.text().catch(() => null);
        let message = `Error ${res.status}`;
        if (text) {
            try {
                const err = JSON.parse(text);
                message = err?.errores?.[0] ?? err?.mensaje ?? message;
            } catch {
                message = text;
            }
        }
        throw new Error(message);
    }
    const text = await res.text();
    return (text ? JSON.parse(text) : null) as T;
}

// RequestInit para peticiones con cuerpo JSON (POST, PUT, PATCH con body).
function jsonInit(method: string, body?: unknown): RequestInit {
    return {
        method,
        headers: { "Content-Type": "application/json", ...authHeader() },
        ...(body !== undefined && { body: JSON.stringify(body) }),
    };
}

// RequestInit para peticiones de solo lectura (GET).
function getInit(): RequestInit {
    return { headers: authHeader() };
}

// ================================================================
// CONTRATOS
// ================================================================

export const contratosApi = {

    // GET  /api/contratos — lista todos los contratos visibles para el rol actual
    listarTodos: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/contratos`, getInit()).then(r => handleResponse(r)),

    // GET  /api/contratos/:id
    obtenerPorId: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}`, getInit()).then(r => handleResponse(r)),

    // POST /api/contratos — crea un contrato nuevo en estado CAPTURA
    crear: (payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos`, jsonInit("POST", payload)).then(r => handleResponse(r)),

    // PUT  /api/contratos/:id — reemplaza todos los campos del contrato (upsert de bienes y claves)
    actualizar: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}`, jsonInit("PUT", payload)).then(r => handleResponse(r)),

    // PATCH /api/contratos/:id/fecha-tentativa — actualiza solo la fecha tentativa de llegada
    actualizarFechaTentativa: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/fecha-tentativa`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),

    // PATCH /api/contratos/:id/enviar-almacen — transición CAPTURA → POR_RECIBIR
    enviarAlmacen: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/enviar-almacen`, jsonInit("PATCH")).then(r => handleResponse(r)),

    // PATCH /api/contratos/:id/autorizar-entrega — transición EN_ALMACEN → LISTO_PARA_ENTREGAR
    autorizarEntrega: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/autorizar-entrega`, jsonInit("PATCH")).then(r => handleResponse(r)),

    // GET   /api/contratos/:id/recepciones — lista todas las recepciones del contrato (más reciente primero)
    listarRecepciones: (id: number): Promise<any[]> =>
        fetch(`${BASE_URL}/api/contratos/${id}/recepciones`, getInit()).then(r => handleResponse(r)),

    // GET   /api/contratos/:id/recepcion — recupera la recepción guardada si existe
    obtenerRecepcion: (id: number): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/recepcion`, getInit()).then(r => handleResponse(r)),

    // POST  /api/contratos/:id/recepcion — registra la recepción física de bienes en almacén
    registrarRecepcion: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/recepcion`, jsonInit("POST", payload)).then(r => handleResponse(r)),

    // GET  /api/contratos/:id/almacen-bienes — bienes con su estado en almacén (para vista de recepción)
    obtenerBienesAlmacen: (id: number, recepcionId?: number): Promise<any[]> => {
        const url = recepcionId != null
            ? `${BASE_URL}/api/contratos/${id}/almacen-bienes?recepcionId=${recepcionId}`
            : `${BASE_URL}/api/contratos/${id}/almacen-bienes`;
        return fetch(url, getInit()).then(r => handleResponse(r));
    },

    // GET  /api/contratos/:id/bienes-entrega — bienes listos para entrega al beneficiario
    obtenerBienesEntrega: (id: number): Promise<any[]> =>
        fetch(`${BASE_URL}/api/contratos/${id}/bienes-entrega`, getInit()).then(r => handleResponse(r)),

    // POST /api/contratos/:id/entrega — registra la entrega al beneficiario
    registrarEntrega: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/contratos/${id}/entrega`, jsonInit("POST", payload)).then(r => handleResponse(r)),
};

// ================================================================
// ALMACÉN BIENES
// Operaciones sobre bienes individuales dentro de un contrato
// una vez que están en almacén.
// ================================================================

export const almacenBienesApi = {

    // PATCH /api/almacen-bienes/:id/datos — guarda número de serie, ubicación y otros datos de recepción
    guardarDatos: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/almacen-bienes/${id}/datos`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),

    // PATCH /api/almacen-bienes/:id/procesar — marca un bien individual como procesado
    procesarUnidad: (id: number, payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/almacen-bienes/${id}/procesar`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),

    // PATCH /api/almacen-bienes/procesar-bloque — procesa múltiples bienes en una sola llamada
    procesarBloque: (payload: unknown): Promise<any> =>
        fetch(`${BASE_URL}/api/almacen-bienes/procesar-bloque`, jsonInit("PATCH", payload)).then(r => handleResponse(r)),
};

// ================================================================
// CATÁLOGOS
// Listas de referencia de solo lectura usadas en formularios.
// ================================================================

export const catalogosApi = {

    // GET /api/claves-presupuestales
    obtenerClavesPresupuestales: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/claves-presupuestales`, getInit()).then(r => handleResponse(r)),

    // GET /api/unidadesMedida
    obtenerUnidadesMedida: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/unidadesMedida`, getInit()).then(r => handleResponse(r)),

    // GET /api/funcionarios — devuelve titulares y administradores de contrato
    obtenerFuncionarios: (): Promise<any[]> =>
        fetch(`${BASE_URL}/api/funcionarios`, getInit()).then(r => handleResponse(r)),
};
