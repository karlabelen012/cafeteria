import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { CartProvider } from './context/CartContext.jsx';
import App from './App.jsx';
import './index.css';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

async function bootstrap() {
  let AppTree;

  if (authDisabled) {
    // Modo noauth: MSAL nunca se instancia (evita el error crypto_nonexistent en HTTP).
    AppTree = (
      <React.StrictMode>
        <CartProvider>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </CartProvider>
      </React.StrictMode>
    );
  } else {
    // Modo con Azure Entra ID: MSAL se inicializa solo cuando hay HTTPS disponible.
    const { PublicClientApplication, EventType } = await import('@azure/msal-browser');
    const { MsalProvider } = await import('@azure/msal-react');
    const { msalConfig } = await import('./auth/authConfig');

    const msalInstance = new PublicClientApplication(msalConfig);

    if (!msalInstance.getActiveAccount() && msalInstance.getAllAccounts().length > 0) {
      msalInstance.setActiveAccount(msalInstance.getAllAccounts()[0]);
    }

    msalInstance.addEventCallback((event) => {
      if (event.eventType === EventType.LOGIN_SUCCESS && event.payload?.account) {
        msalInstance.setActiveAccount(event.payload.account);
      }
    });

    AppTree = (
      <React.StrictMode>
        <MsalProvider instance={msalInstance}>
          <CartProvider>
            <BrowserRouter>
              <App />
            </BrowserRouter>
          </CartProvider>
        </MsalProvider>
      </React.StrictMode>
    );
  }

  ReactDOM.createRoot(document.getElementById('root')).render(AppTree);
}

bootstrap();

