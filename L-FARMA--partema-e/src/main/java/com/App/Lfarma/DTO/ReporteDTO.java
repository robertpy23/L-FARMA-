package com.App.Lfarma.DTO;

import java.util.Date;

public class ReporteDTO {

    private Date fecha;
    private String nombreProducto;
    private int cantidad;
    private double totalVenta;
    private double ganancia;

    public ReporteDTO() {
    }

    public ReporteDTO(Date fecha, String nombreProducto, int cantidad, double totalVenta, double ganancia) {
        this.fecha = fecha;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.totalVenta = totalVenta;
        this.ganancia = ganancia;
    }

    // Getters y Setters
    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public double getGanancia() {
        return ganancia;
    }

    public void setGanancia(double ganancia) {
        this.ganancia = ganancia;
    }

    @Override
    public String toString() {
        return "ReporteDTO{" +
                "fecha=" + fecha +
                ", nombreProducto='" + nombreProducto + '\'' +
                ", cantidad=" + cantidad +
                ", totalVenta=" + totalVenta +
                ", ganancia=" + ganancia +
                '}';
    }
}
