import { useMsal } from '@azure/msal-react';
import { InteractionRequiredAuthError } from '@azure/msal-browser';
import { loginRequest, apiBaseUrl } from '../auth/authConfig';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

// Este hook es el equivalente al MsalInterceptor de Angular: antes de cada
// llamada al BFF, obtiene (o renueva) el access token en silencio y lo
// adjunta como header "Authorization: Bearer <token>".
export function useApiClient() {
  const { instance, accounts } = useMsal();

  async function getToken() {
    const account = accounts[0];
    if (!account) {
      throw new Error('No hay una cuenta autenticada. Inicia sesion primero.');
    }
    try {
      const response = await instance.acquireTokenSilent({
        ...loginRequest,
        account,
      });
      return response.accessToken;
    } catch (error) {
      if (error instanceof InteractionRequiredAuthError) {
        // El token expiro o requiere interaccion: reautentica con redirect.
        await instance.acquireTokenRedirect(loginRequest);
      }
      throw error;
    }
  }

  async function callApi(path, options = {}) {
    // Modo de prueba SIN Azure configurado todavia: no se pide token, y el
    // backend debe estar corriendo con SPRING_PROFILES_ACTIVE=noauth para
    // aceptar la peticion sin JWT. Solo para desarrollo local.
    const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
    if (!authDisabled) {
      const token = await getToken();
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${apiBaseUrl}${path}`, { ...options, headers });

    if (!response.ok) {
      throw new Error(`Error ${response.status} llamando a ${path}`);
    }
    if (response.status === 204) return null;
    return response.json();
  }

  return { callApi };
}
