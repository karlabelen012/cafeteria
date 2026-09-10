import NavBar from './NavBar';
import { useUserRole } from '../hooks/useUserRole';

// Restringe una seccion del portal de staff a ciertos roles (ej. ADMIN).
// El backend ya exige ADMIN para crear/editar/eliminar en estos modulos;
// esto ademas oculta la seccion completa a quien no deberia ni verla.
export default function RequireRole({ allowed, children }) {
  const role = useUserRole();

  if (!allowed.includes(role)) {
    return (
      <div className="app-shell">
        <NavBar />
        <div className="page">
          <div className="state-block">
            <h3>Acceso restringido</h3>
            <p>
              Esta sección es solo para el rol {allowed.join(' o ')}. Tu rol actual es{' '}
              <b>{role || 'sin rol asignado'}</b>.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return children;
}
