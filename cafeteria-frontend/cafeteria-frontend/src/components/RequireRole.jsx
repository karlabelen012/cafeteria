import { useUserRole } from '../hooks/useUserRole';

// Restringe una seccion del dashboard a ciertos roles (ej. ADMIN).
// El backend ya exige el rol correspondiente para cada operación; esto
// además oculta la sección completa a quien no debería ni verla. Vive
// dentro de DashboardLayout, asi que no trae su propio shell.
export default function RequireRole({ allowed, children }) {
  const role = useUserRole();

  if (!allowed.includes(role)) {
    return (
      <div className="state-block">
        <h3>Acceso restringido</h3>
        <p>
          Esta sección es solo para el rol {allowed.join(' o ')}. Tu rol actual es{' '}
          <b>{role || 'sin rol asignado'}</b>.
        </p>
      </div>
    );
  }

  return children;
}
