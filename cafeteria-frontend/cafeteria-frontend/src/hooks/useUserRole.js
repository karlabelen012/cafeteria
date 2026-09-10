import { useMsal } from '@azure/msal-react';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';
export const DEMO_ROLE_KEY = 'cg360_demo_role';

// En produccion (Azure real) el rol viene del claim "roles" del JWT, tal
// como documenta el README. En modo noauth no hay token, asi que se usa un
// selector de rol guardado en localStorage solo para poder demostrar la
// segmentacion por rol en el frontend sin tener Azure configurado.
export function useUserRole() {
  const { accounts } = useMsal();

  if (authDisabled) {
    return localStorage.getItem(DEMO_ROLE_KEY) || 'ADMIN';
  }

  const roles = accounts[0]?.idTokenClaims?.roles || [];
  if (roles.includes('ADMIN')) return 'ADMIN';
  if (roles.includes('BARISTA')) return 'BARISTA';
  if (roles.includes('CAJERO')) return 'CAJERO';
  return roles[0] || null;
}

export function setDemoRole(role) {
  localStorage.setItem(DEMO_ROLE_KEY, role);
}
