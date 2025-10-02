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
    Optional<Producto> findByCodigo(String codigo);
    
    Page<Producto> findByCategoria(String categoria, Pageable pageable);
    
    @Query("{'nombre': {$regex: ?0, $options: 'i'}}")
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    @Query("{'codigo': {$regex: ?0, $options: 'i'}}")
    Page<Producto> findByCodigoContainingIgnoreCase(String codigo, Pageable pageable);
    
    @Query("{ $or: [ "
            + "{'nombre': {$regex: ?0, $options: 'i'}}, "
            + "{'codigo': {$regex: ?0, $options: 'i'}}, "
            + "{'descripcion': {$regex: ?0, $options: 'i'}} "
            + "] }")
    Page<Producto> searchProducts(String searchTerm, Pageable pageable);
    
    @Query("{ $and: [ "
            + "{'categoria': ?0}, "
            + "{ $or: [ "
            + "{'nombre': {$regex: ?1, $options: 'i'}}, "
            + "{'codigo': {$regex: ?1, $options: 'i'}}, "
            + "{'descripcion': {$regex: ?1, $options: 'i'}} "
            + "] } "
            + "] }")
    Page<Producto> searchByCategoryAndTerm(String categoria, String searchTerm, Pageable pageable);
}