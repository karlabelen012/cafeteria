import { Navigate } from 'react-router-dom';
import { useIsAuthenticated } from '@azure/msal-react';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

// Modo con Azure: verifica que haya sesión activa, si no redirige al login.
// Solo se renderiza cuando MsalProvider está en el árbol (authDisabled=false).
function MsalProtectedRoute({ children }) {
  const isAuthenticated = useIsAuthenticated();
  if (!isAuthenticated) {
    return <Navigate to="/portal" replace />;
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

