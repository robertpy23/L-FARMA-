package com.App.Lfarma.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.util.Date;
import java.util.List;

@Data
@Document(collection = "FACTURA")
public class Factura {

    @Id
    private String id;

    private Date fecha;

    private double total;
    private double iva;
    private double totalVenta;
    private double gananciaNeta;

    @DBRef
    private Cliente cliente;

    // Usuario/vendedor que creó la factura (username)
    private String vendedor;

    private List<DetalleFactura> detalles;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva;
    }

    public double getTotalVenta() {
        return totalVenta;
    }

    public void setTotalVenta(double totalVenta) {
        this.totalVenta = totalVenta;
    }

    public double getGananciaNeta() {
        return gananciaNeta;
    }

    public void setGananciaNeta(double gananciaNeta) {
        this.gananciaNeta = gananciaNeta;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public void calcularTotal() {
        double sumaVentas = 0;
        double gananciaTotal = 0;

        if (detalles == null || detalles.isEmpty()) {
            // Si no hay detalles, establecer valores por defecto
            this.totalVenta = 0;
            this.iva = 0;
            this.total = 0;
            this.gananciaNeta = 0;
            return;
        }

        for (DetalleFactura detalle : detalles) {
            // Obtener los valores necesarios
            int cantidad = detalle.getCantidad();
            double precioVenta = detalle.getPrecioUnitario();

            // Verificar que el producto no sea null
            if (detalle.getProducto() == null) {
                System.out.println("⚠️ Advertencia: Detalle sin producto asociado");
                continue;
            }

            double costoCompra = detalle.getProducto().getCostoCompra();

            // Cálculo del total de venta para este detalle
            double totalProducto = precioVenta * cantidad;

            // Cálculo de la ganancia para este detalle
            double gananciaProducto = (precioVenta - costoCompra) * cantidad;

            sumaVentas += totalProducto;
            gananciaTotal += gananciaProducto;

            // Debug - imprimir los valores para verificación
            System.out.println("Producto: " + detalle.getProducto().getNombre());
            System.out.println("Cantidad: " + cantidad);
            System.out.println("Precio Venta: " + precioVenta);
            System.out.println("Costo Compra: " + costoCompra);
            System.out.println("Ganancia por unidad: " + (precioVenta - costoCompra));
            System.out.println("Ganancia total del producto: " + gananciaProducto);
        }

        // SIN IVA: Total = Subtotal (IVA eliminado)
        // Establecer valores
        this.totalVenta = Math.round(sumaVentas * 100.0) / 100.0;
        this.iva = 0; // IVA siempre es 0
        this.total = this.totalVenta; // Total sin IVA agregado
        this.gananciaNeta = Math.round(gananciaTotal * 100.0) / 100.0;

        // Debug - imprimir totales finales
        System.out.println("Subtotal (sin IVA): " + this.totalVenta);
        System.out.println("IVA: " + this.iva);
        System.out.println("Total Venta Final (sin IVA): " + this.total);
        System.out.println("Ganancia Total Final: " + this.gananciaNeta);
    }
}