package com.App.Lfarma.DTO;

public class PrediccionDemandaDTO {
    private String productoId;
    private String codigoProducto;
    private String nombreProducto;
    private String nivelDemanda;
    private double confianza;
    private String colorAlerta;

    // Getters y Setters para todos los campos
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public String getCodigoProducto() { return codigoProducto; }
    public void setCodigoProducto(String codigoProducto) { this.codigoProducto = codigoProducto; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public String getNivelDemanda() { return nivelDemanda; }
    public void setNivelDemanda(String nivelDemanda) { this.nivelDemanda = nivelDemanda; }

    public double getConfianza() { return confianza; }
    public void setConfianza(double confianza) { this.confianza = confianza; }

    public String getColorAlerta() { return colorAlerta; }
    public void setColorAlerta(String colorAlerta) { this.colorAlerta = colorAlerta; }
}