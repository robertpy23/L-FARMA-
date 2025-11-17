package com.App.Lfarma.entity;

import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.Date;

public class DetalleSuministro {

    @DBRef
    private Producto producto;

    private int cantidad;
    private double precioCompra;
    private double precioVentaSugerido;
    private String lote;
    private Date fechaVencimiento;

    // Getters and Setters
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVentaSugerido() { return precioVentaSugerido; }
    public void setPrecioVentaSugerido(double precioVentaSugerido) { this.precioVentaSugerido = precioVentaSugerido; }

    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }

    public double getSubtotal() {
        return this.cantidad * this.precioCompra;
    }
}