import { useEffect, useState } from 'react';
import { useOutletContext } from 'react-router-dom';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  Tooltip,
  CartesianGrid,
  PieChart,
  Pie,
  Cell,
  LineChart,
  Line,
} from 'recharts';
import { useApiClient } from '../../services/apiClient';
import { useAuthProfile } from '../../hooks/useUserRole';
import KanbanBarista from './roles/KanbanBarista.jsx';
import CajaDelDia from './roles/CajaDelDia.jsx';
import InsumosCriticos from './roles/InsumosCriticos.jsx';

const COLOR_ACTUAL = '#5c3420';
const COLOR_ANTERIOR = '#e6cfb5';
const COLORES_FRANJA = ['#a8744a', '#e6cfb5', '#5c3420'];

const formatoCLP = new Intl.NumberFormat('es-CL', { style: 'currency', currency: 'CLP', maximumFractionDigits: 0 });

function nombreDia(fecha) {
  const dias = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'];
  return dias[new Date(fecha + 'T00:00:00').getDay()];
}

function Delta({ valor }) {
  if (valor === null || valor === undefined || Number.isNaN(valor)) return null;
  const subio = valor >= 0;
  return (
    <span className={`dash-kpi__delta ${subio ? 'dash-kpi__delta--up' : 'dash-kpi__delta--down'}`}>
      {subio ? '↑' : '↓'} {Math.abs(valor).toFixed(1)}% vs semana anterior
    </span>
  );
}

function KpiCard({ icono, etiqueta, valor, delta }) {
  return (
    <div className="dash-card">
      <div className="dash-kpi__icon">{icono}</div>
      <p className="dash-kpi__label">{etiqueta}</p>
      <p className="dash-kpi__value">{valor}</p>
      {delta !== undefined && <Delta valor={delta} />}
    </div>
  );
}

function Skeleton({ height = 220 }) {
  return <div className="dash-skeleton" style={{ height }} />;
}

export default function DashboardHome() {
  const { role, nombre } = useAuthProfile();
  const { desde, hasta } = useOutletContext();
  const { callApi } = useApiClient();

  const [datos, setDatos] = useState(null);
  const [alertas, setAlertas] = useState([]);
  const [dlqResumen, setDlqResumen] = useState(null);
  const [colas, setColas] = useState(null);
  const [cluster, setCluster] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [reprocesando, setReprocesando] = useState(null);

  const esVistaKpi = role === 'ADMIN' || role === 'GERENTE';

  useEffect(() => {
    if (!esVistaKpi) {
      setCargando(false);
      return;
    }

    let cancelado = false;
    function cargar() {
      const query = `?desde=${desde}&hasta=${hasta}`;
      const pedidos = [
        callApi(`/reportes/dashboard${query}`).then((d) => !cancelado && setDatos(d)),
        callApi('/notificaciones/alertas').then((a) => !cancelado && setAlertas(a || [])),
      ];
      if (role === 'ADMIN') {
        pedidos.push(callApi('/rabbitmq/dlq').then((d) => !cancelado && setDlqResumen(d || [])));
        pedidos.push(callApi('/rabbitmq/queues').then((q) => !cancelado && setColas(q || [])));
        pedidos.push(callApi('/rabbitmq/cluster').then((c) => !cancelado && setCluster(c)));
      }
      Promise.all(pedidos)
        .catch((err) => {
          console.error(err);
          if (!cancelado) setError('No se pudo cargar el dashboard (revisa el token o el backend).');
        })
        .finally(() => !cancelado && setCargando(false));
    }

    cargar();
    const intervalo = setInterval(cargar, 30000);
    return () => {
      cancelado = true;
      clearInterval(intervalo);
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [desde, hasta, role]);

  async function reprocesarDlq(nombreCola) {
    setReprocesando(nombreCola);
    try {
      await callApi(`/rabbitmq/dlq/${encodeURIComponent(nombreCola)}/reprocess?max=10`, { method: 'POST' });
      const nuevoResumen = await callApi('/rabbitmq/dlq');
      setDlqResumen(nuevoResumen || []);
    } catch (err) {
      console.error(err);
    } finally {
      setReprocesando(null);
    }
  }

  if (!esVistaKpi) {
    return (
      <div>
        <div className="dash__greeting">
          <h1>Bienvenido de vuelta, {nombre || 'equipo'}</h1>
          <p>Así va la cafetería hoy</p>
        </div>
        {role === 'BARISTA' && <KanbanBarista />}
        {role === 'CAJERO' && <CajaDelDia />}
        {role === 'BODEGUERO' && <InsumosCriticos />}
        {!['BARISTA', 'CAJERO', 'BODEGUERO'].includes(role) && (
          <div className="dash-card">
            <p className="dash-empty">Tu rol no tiene una vista asignada todavía.</p>
          </div>
        )}
      </div>
    );
  }

  const totalDlq = (dlqResumen || []).reduce((acc, d) => acc + d.mensajes, 0);
  const ventasChart = (datos?.ventasUltimos7Dias || []).map((v, i) => ({
    fecha: nombreDia(v.fecha),
    actual: v.totalVentas,
    anterior: datos?.ventasSemanaAnterior?.[i]?.totalVentas ?? 0,
  }));

  return (
    <div>
      <div className="dash__greeting">
        <h1>Bienvenido de vuelta, {nombre || 'equipo'}</h1>
        <p>Así va la cafetería hoy</p>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      {cargando && !datos ? (
        <div className="dash__kpi-row">
          {Array.from({ length: 5 }).map((_, i) => (
            <Skeleton key={i} height={110} />
          ))}
        </div>
      ) : (
        datos && (
          <div className="dash__kpi-row">
            <KpiCard icono="💰" etiqueta="Ventas hoy" valor={formatoCLP.format(datos.ventasHoy)}
              delta={datos.variacionVentasSemanaPorcentaje} />
            <KpiCard icono="🧾" etiqueta="Pedidos hoy" valor={datos.pedidosHoy} />
            <KpiCard icono="🎯" etiqueta="Ticket promedio" valor={formatoCLP.format(datos.ticketPromedio)} />
            <KpiCard icono="🙋" etiqueta="Clientes nuevos" valor={datos.clientesNuevosHoy} />
            {role === 'ADMIN' && (
              <KpiCard icono="☠️" etiqueta="Mensajes en DLQ" valor={totalDlq} />
            )}
          </div>
        )
      )}

      <div className="dash__row">
        <div className="dash-card">
          <div className="dash-card__header">
            <div>
              <h3>Ventas últimos 7 días</h3>
              <span className="dash-card__sub">Esta semana vs. semana anterior</span>
            </div>
            <div className="dash-legend">
              <span><i className="dash-legend__dot" style={{ background: COLOR_ACTUAL }} />Actual</span>
              <span><i className="dash-legend__dot" style={{ background: COLOR_ANTERIOR }} />Anterior</span>
            </div>
          </div>
          {cargando && !datos ? (
            <Skeleton />
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <BarChart data={ventasChart} barGap={2}>
                <CartesianGrid vertical={false} stroke="#efe4d8" />
                <XAxis dataKey="fecha" tick={{ fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis hide />
                <Tooltip formatter={(v) => formatoCLP.format(v)} />
                <Bar dataKey="anterior" fill={COLOR_ANTERIOR} radius={[4, 4, 0, 0]} />
                <Bar dataKey="actual" fill={COLOR_ACTUAL} radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="dash-card">
          <div className="dash-card__header">
            <h3>Pedidos por franja</h3>
          </div>
          {cargando && !datos ? (
            <Skeleton />
          ) : (
            <ResponsiveContainer width="100%" height={220}>
              <PieChart>
                <Pie
                  data={datos?.pedidosPorFranja || []}
                  dataKey="cantidad"
                  nameKey="franja"
                  innerRadius={50}
                  outerRadius={80}
                >
                  {(datos?.pedidosPorFranja || []).map((_, i) => (
                    <Cell key={i} fill={COLORES_FRANJA[i % COLORES_FRANJA.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>

      <div className="dash__row--tres">
        <div className="dash-card">
          <div className="dash-card__header">
            <h3>Productos más pedidos</h3>
          </div>
          {datos?.topProductos?.length ? (
            <ul className="dash-list">
              {datos.topProductos.map((p) => (
                <li className="dash-list__item" key={p.productoId}>
                  <span className="dash-list__thumb">☕</span>
                  <div className="dash-list__title">
                    <strong>{p.nombreProducto}</strong>
                    <span>{p.cantidad} vendidos</span>
                  </div>
                  <strong>{formatoCLP.format(p.monto)}</strong>
                </li>
              ))}
            </ul>
          ) : (
            <p className="dash-empty">Sin ventas en el rango seleccionado.</p>
          )}
        </div>

        <div className="dash-card">
          <div className="dash-card__header">
            <h3>Pedidos por hora hoy</h3>
          </div>
          {cargando && !datos ? (
            <Skeleton height={160} />
          ) : (
            <ResponsiveContainer width="100%" height={160}>
              <LineChart data={datos?.pedidosPorHoraHoy || []}>
                <XAxis dataKey="hora" tick={{ fontSize: 11 }} axisLine={false} tickLine={false} />
                <YAxis hide />
                <Tooltip />
                <Line type="monotone" dataKey="cantidad" stroke={COLOR_ACTUAL} strokeWidth={2} dot={false} />
              </LineChart>
            </ResponsiveContainer>
          )}
        </div>

        <div className="dash-card">
          <div className="dash-card__header">
            <h3>Alertas</h3>
          </div>
          {alertas.length ? (
            <ul className="dash-list">
              {alertas.slice(0, 6).map((a) => (
                <li
                  key={a.id}
                  className={`dash-alert dash-alert--${a.tipo === 'STOCK_BAJO' ? 'stock' : a.tipo === 'DLQ' ? 'dlq' : 'pedido'}`}
                >
                  {a.mensaje}
                </li>
              ))}
            </ul>
          ) : (
            <p className="dash-empty">Sin alertas pendientes.</p>
          )}
        </div>
      </div>

      {role === 'ADMIN' && (
        <div className="dash-card">
          <div className="dash-card__header">
            <h3>Estado de mensajería</h3>
            <span className="dash-card__sub">
              Cluster: {cluster?.nodos?.filter((n) => n.running).length ?? '—'}/{cluster?.nodos?.length ?? '—'} nodos activos
            </span>
          </div>
          {colas ? (
            <div className="dash-table-wrap">
              <table className="dash-table">
                <thead>
                  <tr>
                    <th>Cola</th>
                    <th>Listos</th>
                    <th>No confirmados</th>
                    <th>Consumidores</th>
                    <th></th>
                  </tr>
                </thead>
                <tbody>
                  {colas.map((c) => {
                    const esDlq = c.name.endsWith('.dlq');
                    return (
                      <tr key={c.name} style={esDlq && c.messagesReady > 0 ? { color: '#c0392b' } : undefined}>
                        <td>{c.name}</td>
                        <td>{c.messagesReady}</td>
                        <td>{c.messagesUnacknowledged}</td>
                        <td>{c.consumers}</td>
                        <td>
                          {esDlq && c.messagesReady > 0 && (
                            <button
                              className="dash-icon-btn"
                              disabled={reprocesando === c.name}
                              onClick={() => reprocesarDlq(c.name)}
                            >
                              {reprocesando === c.name ? 'Reprocesando…' : 'Reprocesar'}
                            </button>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <Skeleton height={160} />
          )}
        </div>
      )}
    </div>
  );
}
