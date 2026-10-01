import { LogLevel } from '@azure/msal-browser';

// Scopes que el frontend pide al hacer login y al pedir tokens para el backend
// (deben coincidir con los que expusiste en "Expose an API" del App
// Registration del backend, ver seccion 2.2 del README).
export const apiScopes = (import.meta.env.VITE_API_SCOPES || '').split(',').filter(Boolean);

export const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Authority del tenant: por defecto se arma la clasica de Azure AD
// (login.microsoftonline.com/<TENANT_ID>), pero un tenant Microsoft Entra
// External ID (dominio *.ciamlogin.com) usa su propio dominio vanity en vez
// de login.microsoftonline.com — en ese caso hay que setear VITE_AZURE_AUTHORITY
// explicitamente (p.ej. "https://cafegestion360.ciamlogin.com/").
const authority =
  import.meta.env.VITE_AZURE_AUTHORITY || `https://login.microsoftonline.com/${import.meta.env.VITE_AZURE_TENANT_ID}`;

// Configuracion de la instancia MSAL: quien soy (clientId) y en que tenant
// vivo (authority). Estos valores NO son secretos (son publicos en un SPA),
// pero igual se cargan desde .env para no hardcodearlos en el codigo.
export const msalConfig = {
  auth: {
    clientId: import.meta.env.VITE_AZURE_CLIENT_ID,
    authority,
    // MSAL solo confia automaticamente en dominios *.microsoftonline.com; una
    // authority con otro dominio (como ciamlogin.com) hay que declararla aqui
    // explicitamente o MSAL rechaza la autoridad en tiempo de ejecucion.
    knownAuthorities: [new URL(authority).host],
    redirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI || 'http://localhost:4200',
    postLogoutRedirectUri: import.meta.env.VITE_AZURE_REDIRECT_URI || 'http://localhost:4200',
  },
  cache: {
    cacheLocation: 'localStorage', // sobrevive recargas de pagina
    storeAuthStateInCookie: false,
  },
  system: {
    loggerOptions: {
      loggerCallback: (level, message) => {
        if (level === LogLevel.Error) {
          console.error(message);
        }
      },
      logLevel: LogLevel.Warning,
    },
  },
};

// Request usado tanto para el login inicial como para pedir el access token
// que se adjunta a las llamadas al BFF.
export const loginRequest = {
  scopes: apiScopes,
};
