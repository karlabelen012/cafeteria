import { Navigate, useLocation } from 'react-router-dom';
import { useIsAuthenticated, useMsal } from '@azure/msal-react';
import { InteractionStatus } from '@azure/msal-browser';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

// Modo con Azure: verifica que haya sesion activa. Mientras MSAL todavia esta
// resolviendo un redirect/login en curso (inProgress !== "none") se muestra
// un loader en vez de redirigir: redirigir antes de tiempo manda al usuario
// de vuelta a /login aunque la sesion SI vaya a quedar activa en un instante
// (ver docs/EP2_PLAN.md seccion 6.1).
function MsalProtectedRoute({ children }) {
  const isAuthenticated = useIsAuthenticated();
  const { inProgress } = useMsal();
  const location = useLocation();

  if (inProgress !== InteractionStatus.None) {
    return (
      <div className="state-block">
        <div className="spinner" />
        <p>Verificando sesión...</p>
      </div>
    );
  }

  if (!isAuthenticated) {
    const redirect = encodeURIComponent(location.pathname + location.search);
    return <Navigate to={`/login?redirect=${redirect}`} replace />;
  }

  return children;
}

// Modo noauth: deja pasar siempre sin verificar autenticación.
// VITE_AUTH_DISABLED=true — solo para desarrollo/demo, nunca en producción con Azure.
function NoAuthProtectedRoute({ children }) {
  return children;
}

// authDisabled es una constante de build (Vite la hornea en el bundle).
// Cuando es true  → solo NoAuthProtectedRoute se renderiza (MsalProvider no está en el árbol).
// Cuando es false → solo MsalProtectedRoute se renderiza (MsalProvider sí está en el árbol).
export default authDisabled ? NoAuthProtectedRoute : MsalProtectedRoute;
