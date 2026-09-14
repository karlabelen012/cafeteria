import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { loginRequest } from '../auth/authConfig';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';
export const DEMO_ROLE_KEY = 'cg360_demo_role';

function pickRole(roles) {
  if (!roles) return null;
  if (roles.includes('ADMIN')) return 'ADMIN';
  if (roles.includes('BARISTA')) return 'BARISTA';
  if (roles.includes('CAJERO')) return 'CAJERO';
  return roles[0] || null;
}

// Modo noauth: no hay token, se usa un selector de rol guardado en
// localStorage solo para poder demostrar la segmentacion por rol en el
// frontend sin tener Azure configurado.
function useDemoRole() {
  return localStorage.getItem(DEMO_ROLE_KEY) || 'ADMIN';
}

// El rol viene del claim "roles" del ACCESS TOKEN (el mismo que valida el
// backend en @PreAuthorize via JwtGrantedAuthoritiesConverter), NO del ID
// token: los App Roles se asignan en el Enterprise Application del BACKEND,
// asi que solo aparecen en un token pedido con el scope del backend (el
// access token) -- nunca en el ID token, que se emite para el frontend.
function useAzureRole() {
  const { instance, accounts } = useMsal();
  const [role, setRole] = useState(null);

  useEffect(() => {
    const account = accounts[0];
    if (!account) {
      setRole(null);
      return;
    }
    let cancelled = false;
    instance.acquireTokenSilent({ ...loginRequest, account })
      .then((response) => {
        if (cancelled) return;
        const payload = JSON.parse(atob(response.accessToken.split('.')[1]));
        setRole(pickRole(payload.roles));
      })
      .catch(() => {
        if (!cancelled) setRole(null);
      });
    return () => {
      cancelled = true;
    };
  }, [instance, accounts]);

  return role;
}

export const useUserRole = authDisabled ? useDemoRole : useAzureRole;

export function setDemoRole(role) {
  localStorage.setItem(DEMO_ROLE_KEY, role);
}
