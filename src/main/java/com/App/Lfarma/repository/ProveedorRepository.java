package com.App.Lfarma.repository;

import com.App.Lfarma.entity.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends MongoRepository<Proveedor, String> {

    Optional<Proveedor> findByCodigo(String codigo);
    Optional<Proveedor> findByEmail(String email);

    Page<Proveedor> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    Page<Proveedor> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);

    @Query("{ '$or': [ " +
            "{ 'nombre': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'codigo': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'email': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'telefono': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<Proveedor> buscarProveedores(String searchTerm, Pageable pageable);

    Page<Proveedor> findByActivoTrue(Pageable pageable);
    long countByActivoTrue();
}