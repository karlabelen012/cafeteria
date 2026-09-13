import { useMsal } from '@azure/msal-react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { loginRequest, apiBaseUrl } from '../auth/authConfig';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

// ── Modo noauth: no usa MSAL, no hay token, el backend debe correr con noauth ──
function useApiClientNoAuth() {
  async function callApi(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers });
    if (!response.ok) {
      throw new Error(`Error ${response.status} llamando a ${path}`);
    }
    if (response.status === 204) return null;
    return response.json();
  }
  return { callApi };
}

// ── Modo Azure: adjunta el Bearer token de MSAL a cada petición ───────────────
// Este hook es el equivalente al MsalInterceptor de Angular: antes de cada
// llamada al BFF, obtiene (o renueva) el access token en silencio y lo
// adjunta como header "Authorization: Bearer <token>".
function useApiClientMsal() {
  const { instance, accounts } = useMsal();

  async function getToken() {
    const account = accounts[0];
    // Sin sesion: se llama sin token (rutas publicas como el menu de la
    // tienda). Las rutas que si exigen JWT devuelven 401 igual, y el UI
    // (ProtectedRoute) ya evita que un usuario sin sesion llegue ahi.
    if (!account) return null;
    try {
      const response = await instance.acquireTokenSilent({ ...loginRequest, account });
      console.log('TOKEN OBTENIDO:', response.accessToken ? response.accessToken.substring(0, 50) + '...' : 'SIN TOKEN');
      return response.accessToken;
    } catch (error) {
      if (error instanceof InteractionRequiredAuthError) {
        await instance.acquireTokenRedirect(loginRequest);
      }
      throw error;
    }
  }

  async function callApi(path, options = {}) {
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    const token = await getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }
    console.log('HEADERS ENVIADOS:', headers);
    const response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers });
    if (!response.ok) {
      throw new Error(`Error ${response.status} llamando a ${path}`);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  return { callApi };
}

// authDisabled es una constante de build → Vite elimina el código muerto.
export const useApiClient = authDisabled ? useApiClientNoAuth : useApiClientMsal;

