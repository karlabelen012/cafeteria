package cl.duoc.cafeteria.pedidos.service;

import java.util.Map;
import java.util.Set;

/**
 * Maquina de estados de un pedido (ver docs/EP2_PLAN.md seccion 5):
 *
 * PENDIENTE_PAGO -> PAGADO -> EN_PREPARACION -> LISTO -> ENTREGADO
 * PENDIENTE_PAGO -> PAGO_RECHAZADO
 * PAGADO|EN_PREPARACION -> CANCELADO (el controller exige rol ADMIN para esta transicion)
 *
 * Una transicion fuera de esta tabla es un 409 (ConflictoDeNegocioException),
 * resuelto por PedidoServiceImpl.
 */
public final class MaquinaEstadosPedido {

    public static final String PENDIENTE_PAGO = "PENDIENTE_PAGO";
    public static final String PAGADO = "PAGADO";
    public static final String PAGO_RECHAZADO = "PAGO_RECHAZADO";
    public static final String EN_PREPARACION = "EN_PREPARACION";
    public static final String LISTO = "LISTO";
    public static final String ENTREGADO = "ENTREGADO";
    public static final String CANCELADO = "CANCELADO";

    private static final Map<String, Set<String>> TRANSICIONES = Map.of(
            PENDIENTE_PAGO, Set.of(PAGADO, PAGO_RECHAZADO),
            PAGADO, Set.of(EN_PREPARACION, CANCELADO),
            EN_PREPARACION, Set.of(LISTO, CANCELADO),
            LISTO, Set.of(ENTREGADO),
            ENTREGADO, Set.of(),
            PAGO_RECHAZADO, Set.of(),
            CANCELADO, Set.of());

    private MaquinaEstadosPedido() {
    }

    public static boolean puedeTransicionarA(String estadoActual, String nuevoEstado) {
        Set<String> siguientes = TRANSICIONES.get(estadoActual);
        return siguientes != null && siguientes.contains(nuevoEstado);
    }
}
