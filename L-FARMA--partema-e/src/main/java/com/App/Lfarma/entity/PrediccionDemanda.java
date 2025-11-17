package com.App.Lfarma.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Document(collection = "predicciones_demanda")
public class PrediccionDemanda {

    @Id
    private String id;

    private String productoId;
    private String codigoProducto;
    private String nombreProducto;
    private double precio;
    private int cantidad;
    private double precioUnitario;
    private String nivelDemanda;
    private Date fechaPrediccion;
    private double confianza;

    public PrediccionDemanda() {}

    public PrediccionDemanda(String productoId, String codigoProducto, String nombreProducto,
                             double precio, int cantidad, double precioUnitario,
                             String nivelDemanda, double confianza) {
        this.productoId = productoId;
        this.codigoProducto = codigoProducto;
        this.nombreProducto = nombreProducto;
        this.precio = precio;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.nivelDemanda = nivelDemanda;
        this.confianza = confianza;
        this.fechaPrediccion = new Date();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }

    public double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = precioUnitario; }

    public String getNivelDemanda() { return nivelDemanda; }
    public void setNivelDemanda(String nivelDemanda) { this.nivelDemanda = nivelDemanda; }

    public Date getFechaPrediccion() { return fechaPrediccion; }
    public void setFechaPrediccion(Date fechaPrediccion) { this.fechaPrediccion = fechaPrediccion; }

    public double getConfianza() { return confianza; }
    public void setConfianza(double confianza) { this.confianza = confianza; }
}