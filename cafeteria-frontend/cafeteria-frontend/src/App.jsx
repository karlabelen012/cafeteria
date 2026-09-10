import { Routes, Route } from 'react-router-dom';
import Store from './pages/Store.jsx';
import Checkout from './pages/Checkout.jsx';
import OrderStatus from './pages/OrderStatus.jsx';
import StaffLogin from './pages/StaffLogin.jsx';
import Pedidos from './pages/Pedidos.jsx';
import Productos from './pages/Productos.jsx';
import Inventario from './pages/Inventario.jsx';
import Clientes from './pages/Clientes.jsx';
import Empleados from './pages/Empleados.jsx';
import Proveedores from './pages/Proveedores.jsx';
import Pagos from './pages/Pagos.jsx';
import Reportes from './pages/Reportes.jsx';
import ProtectedRoute from './auth/ProtectedRoute.jsx';
import RequireRole from './components/RequireRole.jsx';

function staff(element, roles) {
  const content = roles ? <RequireRole allowed={roles}>{element}</RequireRole> : element;
  return <ProtectedRoute>{content}</ProtectedRoute>;
}

export default function App() {
  return (
    <Routes>
      {/* Tienda publica: sin login, para clientes */}
      <Route path="/" element={<Store />} />
      <Route path="/checkout" element={<Checkout />} />
      <Route path="/pedido/:id" element={<OrderStatus />} />

      {/* Portal de staff: requiere sesion (Azure Entra ID / MSAL) */}
      <Route path="/portal" element={<StaffLogin />} />
      <Route path="/pedidos" element={staff(<Pedidos />)} />
      <Route path="/productos" element={staff(<Productos />)} />
      <Route path="/clientes" element={staff(<Clientes />)} />
      <Route path="/inventario" element={staff(<Inventario />, ['ADMIN'])} />
      <Route path="/empleados" element={staff(<Empleados />, ['ADMIN'])} />
      <Route path="/proveedores" element={staff(<Proveedores />, ['ADMIN'])} />
      <Route path="/pagos" element={staff(<Pagos />, ['ADMIN', 'CAJERO'])} />
      <Route path="/reportes" element={staff(<Reportes />, ['ADMIN'])} />
    </Routes>
  );
}
