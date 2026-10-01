import { Link, useLocation } from 'react-router-dom';
import { useMsal } from '@azure/msal-react';
import CoffeeIcon from './CoffeeIcon';
import { useAuthProfile, setDemoRole } from '../hooks/useUserRole';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

const GROUPS = [
  {
    label: 'Operación',
    items: [
      { to: '/productos', label: 'Menú', roles: null },
      { to: '/pedidos', label: 'Pedidos', roles: null },
      { to: '/inventario', label: 'Inventario', roles: ['ADMIN'] },
    ],
  },
  {
    label: 'Personas',
    items: [
      { to: '/clientes', label: 'Clientes', roles: null },
      { to: '/empleados', label: 'Empleados', roles: ['ADMIN'] },
      { to: '/proveedores', label: 'Proveedores', roles: ['ADMIN'] },
    ],
  },
  {
    label: 'Negocio',
    items: [
      { to: '/pagos', label: 'Pagos', roles: ['ADMIN', 'CAJERO'] },
      { to: '/reportes', label: 'Reportes', roles: ['ADMIN'] },
    ],
  },
];

export default function NavBar() {
  const { instance } = useMsal();
  const location = useLocation();
  const { roles, role, setActiveRole } = useAuthProfile();

  return (
    <div className="navbar-wrap">
      {authDisabled && <div className="demo-banner">MODO DEMO — el rol se simula, no viene de Azure</div>}
      <header className="navbar">
        <Link to="/portal" className="navbar__brand">
          <CoffeeIcon />
          CafeGestión360
          <span className="navbar__tag">Staff · {role || 'sin rol'}</span>
        </Link>
        <div className="navbar__right">
          {authDisabled && (
            <select
              className="role-switcher"
              value={role}
              onChange={(e) => setActiveRole(e.target.value)}
              title="Simulador de rol (solo modo noauth, sin Azure real)"
            >
              <option value="ADMIN">Ver como: ADMIN</option>
              <option value="GERENTE">Ver como: GERENTE</option>
              <option value="BARISTA">Ver como: BARISTA</option>
              <option value="CAJERO">Ver como: CAJERO</option>
              <option value="BODEGUERO">Ver como: BODEGUERO</option>
            </select>
          )}
          {!authDisabled && roles.length > 1 && (
            <select
              className="role-switcher"
              value={role || ''}
              onChange={(e) => setActiveRole(e.target.value)}
              title="Elegir perfil activo (tienes más de un rol asignado)"
            >
              {roles.map((r) => (
                <option key={r} value={r}>
                  Perfil: {r}
                </option>
              ))}
            </select>
          )}
          <Link to="/" className="btn btn-ghost">
            Ver tienda
          </Link>
          {!authDisabled && (
            <button className="btn btn-ghost" onClick={() => instance.logoutRedirect()}>
              Cerrar sesión
            </button>
          )}
        </div>
      </header>
      <nav className="module-nav module-nav--bar">
        {GROUPS.map((group) => {
          const visibles = group.items.filter((item) => !item.roles || item.roles.includes(role));
          if (visibles.length === 0) return null;
          return (
            <div className="module-nav__group" key={group.label}>
              {visibles.map((item) => (
                <Link
                  key={item.to}
                  to={item.to}
                  className={`module-nav__item${location.pathname === item.to ? ' module-nav__item--active' : ''}`}
                >
                  {item.label}
                </Link>
              ))}
            </div>
          );
        })}
      </nav>
    </div>
  );
}
