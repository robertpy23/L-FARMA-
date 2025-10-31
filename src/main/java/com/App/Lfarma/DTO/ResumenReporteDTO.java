package com.App.Lfarma.DTO;

public class ResumenReporteDTO {
    private double totalVentas;
    private double totalGanancia;
    private long productosVendidos;
    private long facturasEmitidas;

    public ResumenReporteDTO() {}

    public ResumenReporteDTO(double totalVentas, double totalGanancia, long productosVendidos, long facturasEmitidas) {
        this.totalVentas = totalVentas;
        this.totalGanancia = totalGanancia;
        this.productosVendidos = productosVendidos;
        this.facturasEmitidas = facturasEmitidas;
    }

    public double getTotalVentas() { return totalVentas; }
    public void setTotalVentas(double totalVentas) { this.totalVentas = totalVentas; }

    public double getTotalGanancia() { return totalGanancia; }
    public void setTotalGanancia(double totalGanancia) { this.totalGanancia = totalGanancia; }

    public long getProductosVendidos() { return productosVendidos; }
    public void setProductosVendidos(long productosVendidos) { this.productosVendidos = productosVendidos; }

    public long getFacturasEmitidas() { return facturasEmitidas; }
    public void setFacturasEmitidas(long facturasEmitidas) { this.facturasEmitidas = facturasEmitidas; }
}
