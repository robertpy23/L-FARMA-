package com.App.Lfarma.service;

import com.App.Lfarma.entity.Proveedor;
import com.App.Lfarma.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    public Page<Proveedor> listarProveedoresPaginados(Pageable pageable) {
        return proveedorRepository.findAll(pageable);
    }

    public Page<Proveedor> buscarProveedores(String searchTerm, Pageable pageable) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return proveedorRepository.findAll(pageable);
        }
        return proveedorRepository.buscarProveedores(searchTerm.trim(), pageable);
    }

    public Optional<Proveedor> obtenerProveedorPorCodigo(String codigo) {
        return proveedorRepository.findByCodigo(codigo.trim());
    }

    public Optional<Proveedor> obtenerProveedorPorId(String id) {
        return proveedorRepository.findById(id);
    }

    public Proveedor guardarProveedor(Proveedor proveedor) {
        // Validaciones
        if (proveedor.getCodigo() == null || proveedor.getCodigo().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del proveedor es obligatorio");
        }

        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }

        // Limpiar datos
        proveedor.setCodigo(proveedor.getCodigo().trim());
        proveedor.setNombre(proveedor.getNombre().trim());

        if (proveedor.getEmail() != null) {
            proveedor.setEmail(proveedor.getEmail().trim().toLowerCase());
        }

        // Verificar código único
        if (proveedor.getId() == null) {
            // Nuevo proveedor
            Optional<Proveedor> existente = proveedorRepository.findByCodigo(proveedor.getCodigo());
            if (existente.isPresent()) {
                throw new IllegalArgumentException("Ya existe un proveedor con el código: " + proveedor.getCodigo());
            }
            proveedor.setFechaRegistro(new Date());
        } else {
            // Actualización
            Optional<Proveedor> existente = proveedorRepository.findByCodigo(proveedor.getCodigo());
            if (existente.isPresent() && !existente.get().getId().equals(proveedor.getId())) {
                throw new IllegalArgumentException("Ya existe otro proveedor con el código: " + proveedor.getCodigo());
            }
            proveedor.setFechaActualizacion(new Date());
        }

        return proveedorRepository.save(proveedor);
    }

    public void eliminarProveedor(String codigo) {
        Optional<Proveedor> proveedor = proveedorRepository.findByCodigo(codigo);
        if (proveedor.isPresent()) {
            // En lugar de eliminar, desactivar
            Proveedor p = proveedor.get();
            p.setActivo(false);
            p.setFechaActualizacion(new Date());
            proveedorRepository.save(p);
        } else {
            throw new RuntimeException("Proveedor no encontrado: " + codigo);
        }
    }

    public long contarProveedoresActivos() {
        return proveedorRepository.countByActivoTrue();
    }

    public Page<Proveedor> obtenerProveedoresActivos(Pageable pageable) {
        return proveedorRepository.findByActivoTrue(pageable);
    }
}