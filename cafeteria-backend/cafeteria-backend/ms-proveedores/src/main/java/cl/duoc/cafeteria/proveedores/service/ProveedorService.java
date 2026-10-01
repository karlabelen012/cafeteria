package cl.duoc.cafeteria.proveedores.service;

import cl.duoc.cafeteria.proveedores.dto.ProveedorRequest;
import cl.duoc.cafeteria.proveedores.dto.ProveedorResponse;

import java.util.List;

public interface ProveedorService {

    List<ProveedorResponse> listar();

    ProveedorResponse obtener(Long id);

    ProveedorResponse crear(ProveedorRequest request);

    ProveedorResponse actualizar(Long id, ProveedorRequest request);

    void eliminar(Long id);
}
