package com.App.Lfarma.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "CARRITOS")
public class ShoppingCart {

    @Id
    private String id;

    @DBRef
    private Usuario usuario;

    // Lista de productos en el carrito
    private List<ItemCarrito> items = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public List<ItemCarrito> getItems() {
        return items;
    }

    public void setItems(List<ItemCarrito> items) {
        this.items = items;
    }

    // Calcular el total del carrito
    public double getTotal() {
        return items.stream()
                .mapToDouble(ItemCarrito::getSubtotal)
                .sum();
    }
}
