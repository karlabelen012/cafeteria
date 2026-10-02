// Administracion de RabbitMQ (ver docs/EP2_PLAN.md seccion 3.8 y 6.2): crear
// y eliminar colas/exchanges/bindings usando ms-rabbitmq-admin. Se completa
// en la etapa de "vistas por rol + Mensajería" de la Fase 5.
export default function Mensajeria() {
  return (
    <div>
      <div className="dash-page__header">
        <div>
          <h2>Mensajería</h2>
          <p>Colas, exchanges y bindings de RabbitMQ.</p>
        </div>
      </div>
      <div className="dash-card">
        <p className="dash-empty">
          La gestión de colas/exchanges/bindings se agrega pronto. Mientras tanto, el resumen de DLQ y el
          estado del cluster ya están disponibles en el resumen del dashboard.
        </p>
      </div>
    </div>
  );
}
