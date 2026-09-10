import { useEffect, useState } from 'react';
import { useApiClient } from '../services/apiClient';
import NavBar from '../components/NavBar';

const ROLES = ['BARISTA', 'CAJERO', 'ADMIN'];
const VACIO = { nombre: '', email: '', rol: 'BARISTA', activo: true };

export default function Empleados() {
  const { callApi } = useApiClient();
  const [empleados, setEmpleados] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [nuevo, setNuevo] = useState(VACIO);
  const [guardando, setGuardando] = useState(false);

  const cargar = () => {
    setCargando(true);
    callApi('/empleados')
      .then(setEmpleados)
      .catch((err) => {
        console.error(err);
        setError('No se pudo cargar el equipo (revisa el token o el backend).');
      })
      .finally(() => setCargando(false));
  };

  useEffect(cargar, []);

  const crearEmpleado = async (e) => {
    e.preventDefault();
    setGuardando(true);
    try {
      const creado = await callApi('/empleados', {
        method: 'POST',
        body: JSON.stringify(nuevo),
      });
      setEmpleados((prev) => [...prev, creado]);
      setNuevo(VACIO);
    } catch (err) {
      console.error(err);
      setError('No se pudo crear el empleado.');
    } finally {
      setGuardando(false);
    }
  };

  const alternarActivo = async (empleado) => {
    try {
      const actualizado = await callApi(`/empleados/${empleado.id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...empleado, activo: !empleado.activo }),
      });
      setEmpleados((prev) => prev.map((e) => (e.id === empleado.id ? actualizado : e)));
    } catch (err) {
      console.error(err);
      setError(`No se pudo actualizar a ${empleado.nombre}.`);
    }
  };

  const eliminarEmpleado = async (empleado) => {
    try {
      await callApi(`/empleados/${empleado.id}`, { method: 'DELETE' });
      setEmpleados((prev) => prev.filter((e) => e.id !== empleado.id));
    } catch (err) {
      console.error(err);
      setError(`No se pudo eliminar a ${empleado.nombre}.`);
    }
  };

  return (
    <div className="app-shell">
      <NavBar />
      <div className="page">
        <div className="page__header">
          <div>
            <h2>Empleados</h2>
            <p>Equipo de la cafetería, sus roles y estado.</p>
          </div>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <form className="checkout-form" style={{ marginBottom: 28, maxWidth: 560 }} onSubmit={crearEmpleado}>
          <h3>Agregar empleado</h3>
          <div className="checkout-form__row">
            <label>
              Nombre
              <input
                required
                value={nuevo.nombre}
                onChange={(e) => setNuevo((n) => ({ ...n, nombre: e.target.value }))}
              />
            </label>
            <label>
              Email
              <input
                required
                type="email"
                value={nuevo.email}
                onChange={(e) => setNuevo((n) => ({ ...n, email: e.target.value }))}
              />
            </label>
          </div>
          <label>
            Rol
            <select value={nuevo.rol} onChange={(e) => setNuevo((n) => ({ ...n, rol: e.target.value }))}>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </label>
          <button className="btn btn-primary" type="submit" disabled={guardando}>
            {guardando ? 'Guardando...' : 'Agregar empleado'}
          </button>
        </form>

        {cargando && (
          <div className="state-block">
            <div className="spinner" />
            <p>Cargando...</p>
          </div>
        )}

        {!cargando && !error && empleados.length === 0 && (
          <div className="state-block">
            <h3>Todavía no hay empleados registrados</h3>
            <p>Agrega el primer empleado desde el formulario de arriba.</p>
          </div>
        )}

        {!cargando && empleados.length > 0 && (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Nombre</th>
                  <th>Email</th>
                  <th>Rol</th>
                  <th>Estado</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {empleados.map((e) => (
                  <tr key={e.id}>
                    <td>
                      <strong>{e.nombre}</strong>
                    </td>
                    <td>{e.email}</td>
                    <td>
                      <span className="badge">{e.rol}</span>
                    </td>
                    <td>
                      <button
                        type="button"
                        className={`badge ${e.activo ? 'badge--success' : 'badge--neutral'}`}
                        style={{ border: 'none', cursor: 'pointer' }}
                        onClick={() => alternarActivo(e)}
                      >
                        {e.activo ? 'Activo' : 'Inactivo'}
                      </button>
                    </td>
                    <td>
                      <button className="btn btn-ghost btn-sm" onClick={() => eliminarEmpleado(e)}>
                        Eliminar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
