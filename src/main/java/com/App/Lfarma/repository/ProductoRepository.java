package com.App.Lfarma.repository;

import com.App.Lfarma.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends MongoRepository<Producto, String> {

    // ✅ Búsqueda por código exacto
    Optional<Producto> findByCodigo(String codigo);

    // ✅ Búsqueda por categoría con paginación
    Page<Producto> findByCategoria(String categoria, Pageable pageable);

    // ✅ Búsqueda por nombre (case insensitive) con paginación
    @Query("{'nombre': {$regex: ?0, $options: 'i'}}")
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);

    // ✅ Búsqueda por código (case insensitive) con paginación
    @Query("{'codigo': {$regex: ?0, $options: 'i'}}")
    Page<Producto> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);

    // ✅ Búsqueda combinada por nombre y categoría
    @Query("{'nombre': {$regex: ?0, $options: 'i'}, 'categoria': ?1}")
    Page<Producto> findByNombreContainingIgnoreCaseAndCategoria(String nombre, String categoria, Pageable pageable);

    // ✅ Búsqueda general en múltiples campos
    @Query("{ $or: [ "
            + "{'nombre': {$regex: ?0, $options: 'i'}}, "
            + "{'codigo': {$regex: ?0, $options: 'i'}}, "
            + "{'descripcion': {$regex: ?0, $options: 'i'}} "
            + "] }")
    Page<Producto> searchProducts(String searchTerm, Pageable pageable);

    // ✅ Búsqueda por categoría y término en múltiples campos
    @Query("{ $and: [ "
            + "{'categoria': ?0}, "
            + "{ $or: [ "
            + "{'nombre': {$regex: ?1, $options: 'i'}}, "
            + "{'codigo': {$regex: ?1, $options: 'i'}}, "
            + "{'descripcion': {$regex: ?1, $options: 'i'}} "
            + "] } "
            + "] }")
    Page<Producto> searchByCategoryAndTerm(String categoria, String searchTerm, Pageable pageable);

    // ✅ Verificar existencia por código
    boolean existsByCodigo(String codigo);

    // ✅ Búsqueda por stock bajo
    Page<Producto> findByCantidadLessThanEqual(int cantidad, Pageable pageable);

    // ✅ Búsqueda por rango de precio
    @Query("{'precio': {$gte: ?0, $lte: ?1}}")
    Page<Producto> findByPrecioBetween(double precioMin, double precioMax, Pageable pageable);
}