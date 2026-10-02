// Vista BARISTA: "Cola de preparación" (ver docs/EP2_PLAN.md seccion 6.2).
// Placeholder funcional minimo — se completa con el kanban PAGADO -> ENTREGADO
// en la etapa de "vistas por rol" de la Fase 5.
export default function KanbanBarista() {
  return (
    <div className="dash-card">
      <div className="dash-card__header">
        <h3>Cola de preparación</h3>
      </div>
      <p className="dash-empty">El kanban de pedidos (PAGADO → EN_PREPARACION → LISTO → ENTREGADO) se agrega pronto.</p>
    </div>
  );
}
