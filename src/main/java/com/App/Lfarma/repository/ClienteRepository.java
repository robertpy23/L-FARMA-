package com.App.Lfarma.repository;

import com.App.Lfarma.entity.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends MongoRepository<Cliente, String> {
    Optional<Cliente> findByCodigo(String codigo);
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByUsername(String username);

    // ✅ NUEVO: Para búsqueda paginada por nombre
    Page<Cliente> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // ✅ NUEVO: Para búsqueda por código con paginación
    Page<Cliente> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);

    // ✅ NUEVO: Para búsqueda combinada (nombre o código)
    Page<Cliente> findByNombreContainingIgnoreCaseOrCodigoContainingIgnoreCase(String nombre, String codigo, Pageable pageable);

    // ✅ NUEVO: Para búsqueda por teléfono
    Optional<Cliente> findByTelefono(String telefono);

    // ✅ NUEVO: Para búsqueda por teléfono con paginación
    Page<Cliente> findByTelefonoContaining(String telefono, Pageable pageable);

    // ✅ NUEVO: Búsqueda avanzada con múltiples campos
    @Query("{ '$or': [ " +
            "{ 'nombre': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'codigo': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'email': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'telefono': { '$regex': ?0, '$options': 'i' } }, " +
            "{ 'identificacion': { '$regex': ?0, '$options': 'i' } } " +
            "] }")
    Page<Cliente> buscarClientes(String searchTerm, Pageable pageable);

    // ✅ NUEVO: Clientes con dirección completa (para envíos)
    @Query("{ 'direccion': { '$exists': true, '$ne': '' }, " +
            "'latitud': { '$exists': true, '$ne': null }, " +
            "'longitud': { '$exists': true, '$ne': null } }")
    Page<Cliente> findClientesConDireccionCompleta(Pageable pageable);

    // ✅ NUEVO: Contar clientes por estado de dirección
    @Query(value = "{ 'direccion': { '$exists': true, '$ne': '' } }", count = true)
    long countClientesConDireccion();

    @Query(value = "{ 'direccion': { '$exists': false } }", count = true)
    long countClientesSinDireccion();

    @Query(value = "{ 'direccion': { '$exists': true, '$ne': '' }, " +
            "'latitud': { '$exists': true, '$ne': null }, " +
            "'longitud': { '$exists': true, '$ne': null } }", count = true)
    long countClientesConUbicacionCompleta();

    // ✅ NUEVO: Buscar clientes por ciudad o localidad (si tienes ese campo)
    @Query("{ 'direccion': { '$regex': ?0, '$options': 'i' } }")
    Page<Cliente> findByDireccionContainingIgnoreCase(String direccion, Pageable pageable);

    // ✅ NUEVO: Clientes recientes (ordenados por fecha si tienes el campo)
    Page<Cliente> findAllByOrderByCodigoDesc(Pageable pageable);
}