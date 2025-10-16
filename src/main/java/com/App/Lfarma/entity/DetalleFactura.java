package com.App.Lfarma.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

@Document(collection = "detalleVentas")
public class DetalleFactura {

    @Id
    private String id;

    @DBRef
    private Producto producto;

    private int cantidad;
    private double precioUnitario;

    // No necesitamos incluir la referencia a Factura aquí porque
    // la factura tendrá una lista de detalles embebidos
    // En MongoDB, normalmente embebemos los documentos pequeños dentro del documento principal

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    // Método para calcular el subtotal del detalle de la factura
    public double getSubtotal() {
        return this.cantidad * this.precioUnitario;
    }
}