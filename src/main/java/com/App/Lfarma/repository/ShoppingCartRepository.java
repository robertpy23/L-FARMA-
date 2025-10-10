package com.App.Lfarma.repository;

import com.App.Lfarma.entity.ShoppingCart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShoppingCartRepository extends MongoRepository<ShoppingCart, String> {

    // Si en tu ShoppingCart tienes: private Usuario usuario; (DBRef)
    Optional<ShoppingCart> findByUsuario_Id(String usuarioId);

    // Si en tu ShoppingCart tienes: private String usuarioId;
    Optional<ShoppingCart> findByUsuarioId(String usuarioId);

    // eliminar por usuario (ambas variantes)
    void deleteByUsuario_Id(String usuarioId);
    void deleteByUsuarioId(String usuarioId);
}
