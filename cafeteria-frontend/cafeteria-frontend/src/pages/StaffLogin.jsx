import { useNavigate, Link } from 'react-router-dom';
import { useMsal, AuthenticatedTemplate, UnauthenticatedTemplate } from '@azure/msal-react';
import { loginRequest } from '../auth/authConfig';
import CoffeeIcon from '../components/CoffeeIcon';

const authDisabled = import.meta.env.VITE_AUTH_DISABLED === 'true';

export default function StaffLogin() {
  const { instance, accounts } = useMsal();
  const navigate = useNavigate();

  const handleLogin = () => {
    // Dispara el flujo Authorization Code + PKCE contra Azure Entra ID.
    // MSAL redirige al usuario y, al volver, procesa el codigo y obtiene
    // los tokens automaticamente (ver el manejo del redirect en main.jsx).
    instance.loginRedirect(loginRequest);
  };

  const handleLogout = () => {
    instance.logoutRedirect();
  };

  return (
    <div className="app-shell">
      <div className="hero">
        <div className="hero__content">
          <span className="hero__eyebrow">
            <CoffeeIcon size={16} /> Portal del equipo
          </span>
          <h1>Barra, bodega y caja — todo en un solo lugar.</h1>
          <p className="hero__subtitle">
            Acceso para baristas, cajeros y administración: pedidos en curso, inventario,
            pagos y reportes del negocio.
          </p>

          {authDisabled && (
            <div className="alert alert-warning">
              <span>
                Modo de prueba sin Azure configurado (<code>VITE_AUTH_DISABLED=true</code>).
                El backend debe estar corriendo con <code>SPRING_PROFILES_ACTIVE=noauth</code>.
              </span>
            </div>
          )}

          {authDisabled ? (
            <div className="hero__actions">
              <button className="btn btn-primary" onClick={() => navigate('/pedidos')}>
                Entrar al portal
              </button>
              <Link to="/" className="btn btn-outline">
                Ir a la tienda
              </Link>
            </div>
          ) : (
            <>
              <UnauthenticatedTemplate>
                <div className="hero__actions">
                  <button className="btn btn-primary" onClick={handleLogin}>
                    Iniciar sesión
                  </button>
                  <Link to="/" className="btn btn-outline">
                    Ir a la tienda
                  </Link>
                </div>
              </UnauthenticatedTemplate>

              <AuthenticatedTemplate>
                <p className="hero__session">
                  Sesión iniciada como <b>{accounts[0]?.name ?? accounts[0]?.username}</b>
                </p>
                <div className="hero__actions">
                  <button className="btn btn-primary" onClick={() => navigate('/pedidos')}>
                    Entrar al portal
                  </button>
                  <button className="btn btn-ghost" onClick={handleLogout}>
                    Cerrar sesión
                  </button>
                </div>
              </AuthenticatedTemplate>
            </>
          )}
        </div>

        <div className="hero__panel">
          <div className="hero__panel-card">
            <h3>Hoy en la barra</h3>
            <p>
              Cada pedido, cada insumo y cada boleta, trazables en tiempo real — para que el
              dueño ya no dependa de lo que ve parado en el mostrador.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
