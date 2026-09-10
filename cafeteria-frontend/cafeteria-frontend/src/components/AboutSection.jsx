function EyeIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
      <path d="M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7-10-7-10-7Z" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="12" cy="12" r="3" stroke="currentColor" strokeWidth="1.8" />
    </svg>
  );
}

function CompassIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.8" />
      <path d="m14.5 9.5-1.6 5.1-5.1 1.6 1.6-5.1 5.1-1.6Z" stroke="currentColor" strokeWidth="1.6" strokeLinejoin="round" />
    </svg>
  );
}

function TargetIcon() {
  return (
    <svg width="22" height="22" viewBox="0 0 24 24" fill="none">
      <circle cx="12" cy="12" r="9" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="12" cy="12" r="5" stroke="currentColor" strokeWidth="1.8" />
      <circle cx="12" cy="12" r="1.4" fill="currentColor" />
    </svg>
  );
}

const ITEMS = [
  {
    icon: <EyeIcon />,
    title: 'Visión',
    text: 'Ser la plataforma de referencia para que las cafeterías de barrio dejen el papel atrás y operen con datos en tiempo real, sin perder la calidez del trato cercano.',
  },
  {
    icon: <CompassIcon />,
    title: 'Misión',
    text: 'Darle a cada cafetería las herramientas digitales —pedidos, inventario, clientes y reportes— que hoy solo tienen las grandes cadenas, para que ninguna venta ni insumo se pierda en el camino.',
  },
  {
    icon: <TargetIcon />,
    title: 'Objetivo',
    text: 'Reducir a cero los pedidos confundidos, los quiebres de stock sorpresivos y las decisiones a ciegas, dándole al dueño visibilidad real de su negocio todos los días.',
  },
];

export default function AboutSection() {
  return (
    <section className="about-section">
      <div className="about-section__inner">
        <div className="about-section__heading">
          <span>Quiénes somos</span>
          <h2>De la libreta de papel a un negocio con datos reales</h2>
          <p>
            CafeGestión360 nace de una cafetería real que llevaba años administrando pedidos,
            inventario y ventas a mano — con todo lo que eso significa en pedidos perdidos y
            quiebres de stock de última hora.
          </p>
        </div>
        <div className="about-grid">
          {ITEMS.map((item) => (
            <div className="about-card" key={item.title}>
              <div className="about-card__icon">{item.icon}</div>
              <h3>{item.title}</h3>
              <p>{item.text}</p>
            </div>
          ))}
        </div>
      </div>
    </section>
  );
}
