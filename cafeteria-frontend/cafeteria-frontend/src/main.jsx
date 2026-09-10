import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import { PublicClientApplication, EventType } from '@azure/msal-browser';
import { MsalProvider } from '@azure/msal-react';
import { msalConfig } from './auth/authConfig';
import { CartProvider } from './context/CartContext.jsx';
import App from './App.jsx';
import './index.css';

// Una sola instancia de MSAL para toda la app (recomendado por Microsoft).
const msalInstance = new PublicClientApplication(msalConfig);

// Si ya hay una cuenta logueada (ej. tras volver del redirect de login),
// la marcamos como cuenta activa para que MsalProvider sepa cual usar.
if (!msalInstance.getActiveAccount() && msalInstance.getAllAccounts().length > 0) {
  msalInstance.setActiveAccount(msalInstance.getAllAccounts()[0]);
}

msalInstance.addEventCallback((event) => {
  if (event.eventType === EventType.LOGIN_SUCCESS && event.payload?.account) {
    msalInstance.setActiveAccount(event.payload.account);
  }
});

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <MsalProvider instance={msalInstance}>
      <CartProvider>
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </CartProvider>
    </MsalProvider>
  </React.StrictMode>,
);
