import { Navigate } from 'react-router-dom';
import { useIsAuthenticated } from '@azure/msal-react';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

// Equivalente al MsalGuard de Angular: si no hay sesion activa,
// redirige a la pagina de login en vez de mostrar la ruta protegida.
//
// Si VITE_AUTH_DISABLED=true (modo de prueba SIN Azure configurado todavia),
// deja pasar siempre. Esto es solo para desarrollo local mientras se
// integra Azure Entra ID; no debe usarse en la entrega final.
export default function ProtectedRoute({ children }) {
  const isAuthenticated = useIsAuthenticated();

  if (!authDisabled && !isAuthenticated) {
    return <Navigate to="/portal" replace />;
  }

  return children;
}
