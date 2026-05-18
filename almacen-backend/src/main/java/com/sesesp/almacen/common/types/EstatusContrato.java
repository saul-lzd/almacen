//package com.sesesp.almacen.common.types;
//
//import lombok.Getter;
//
//import java.util.Arrays;
//import java.util.Map;
//import java.util.Optional;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//@Getter
//public enum EstatusContrato {
//    EN_CAPTURA(1, 1, "Contrato en captura progresiva"),
//    POR_RECIBIR(2, 2, "Contrato completo y pendiente de recepción"),
//    EN_ALMACEN(3, 3, "Pedido recibido total o parcialmente"),
//    PROCESANDO(4, 4, "Pedido con captura de datos y evidencias en proceso"),
//    LISTO_PARA_ENTREGAR(5, 5, "Productos procesados y listos para entrega a beneficiarios"),
//    ENTREGA_PARCIAL(6, 6, "Entrega parcial realizada al beneficiario"),
//    ENTREGADO(7, 7, "Todos los productos fueron entregados"),
//    CERRADO(8, 8, "Contrato cerrado o archivado");
//
//    private final int id;
//    private final int order;
//    private final String description;
//
//    EstatusContrato(int id, int order, String description) {
//        this.id = id;
//        this.order = order;
//        this.description = description;
//    }
//
//
//    /**
//     * Recupera un EstatusContrato a partir de su ID numérico.
//     *
//     * @param id Identificador del estatus.
//     * @return Un Optional que contiene el estatus si se encuentra.
//     */
//    public static Optional<EstatusContrato> fromId(int id) {
//        return Optional.ofNullable(LOOKUP.get(id));
//    }
//
//    // Cache para búsqueda rápida por ID (Evita iterar el array cada vez)
//    private static final Map<Integer, EstatusContrato> LOOKUP =
//            Arrays.stream(values())
//                    .collect(Collectors
//                            .toUnmodifiableMap(EstatusContrato::getId, Function.identity()));
//
//
//}
