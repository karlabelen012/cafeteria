import { useEffect, useState } from 'react';
import { useMsal } from '@azure/msal-react';
import { loginRequest } from '../auth/authConfig';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';
export const DEMO_ROLE_KEY = 'cg360_demo_role';

// Decodifica el payload de un JWT (base64url) sin librerias externas.
// Solo lee el claim "roles", no valida firma (la validacion real la hace
// el backend; aqui solo es para pintar la UI).
function decodeRolesFromJwt(jwt) {
  try {
    const payload = jwt.split('.')[1];
    const base64 = payload.replace(/-/g, '+').replace(/_/g, '/');
    const json = decodeURIComponent(
      atob(base64)
        .split('')
        .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
        .join('')
    );
    return JSON.parse(json).roles || [];
  } catch {
    return [];
  }
}

function pickRole(roles) {
  if (roles.includes('ADMIN')) return 'ADMIN';
  if (roles.includes('BARISTA')) return 'BARISTA';
  if (roles.includes('CAJERO')) return 'CAJERO';
  return roles[0] || null;
}

// En produccion (Azure real) el rol viene del claim "roles", pero ese claim
// solo esta garantizado en el ACCESS TOKEN (el que se usa para llamar al
// BFF/API Gateway), no en el ID token. Por eso hay que pedirlo con
// acquireTokenSilent igual que hace apiClient.js, y decodificarlo.
// En modo noauth no hay token, asi que se usa un selector de rol guardado
// en localStorage solo para poder demostrar la segmentacion por rol en el
// frontend sin tener Azure configurado.
export function useUserRole() {
  const { instance, accounts } = useMsal();
  const [role, setRole] = useState(() =>
    authDisabled ? localStorage.getItem(DEMO_ROLE_KEY) || 'ADMIN' : null
  );

  useEffect(() => {
    if (authDisabled) return;
    const account = accounts[0];
    if (!account) return;

    let cancelled = false;
    instance
      .acquireTokenSilent({ ...loginRequest, account })
      .then((response) => {
        if (cancelled) return;
        setRole(pickRole(decodeRolesFromJwt(response.accessToken)));
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

export function setDemoRole(role) {
  localStorage.setItem(DEMO_ROLE_KEY, role);
}
