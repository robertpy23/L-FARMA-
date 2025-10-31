package com.App.Lfarma.entity;

import jakarta.persistence.Entity;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.time.LocalDate;
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

    // En MongoDB, podemos embeber los detalles directamente en la factura
    // o usar referencias con @DBRef
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<DetalleFactura> getDetalles() {
        return detalles;
    }
    public double getGananciaNeta() {
    return gananciaNeta;
   }

    public void setGananciaNeta(double gananciaNeta) {
      this.gananciaNeta = gananciaNeta;
    }




    public void setDetalles(List<DetalleFactura> detalles) {
        this.detalles = detalles;
    }

    public void calcularTotal() {
        double sumaVentas = 0;
        double gananciaTotal = 0;
        
        for (DetalleFactura detalle : detalles) {
            // Obtener los valores necesarios
            int cantidad = detalle.getCantidad();
            double precioVenta = detalle.getPrecioUnitario();
            double costoCompra = detalle.getProducto().getCostoCompra();
            
            // Cálculo del total de venta para este detalle
            double totalProducto = precioVenta * cantidad;
            
            // Cálculo de la ganancia para este detalle
            // Ganancia = (Precio Venta - Costo Compra) * Cantidad
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
        
        // Establecer total (sin IVA)
        this.total = sumaVentas;
        this.totalVenta = sumaVentas;
        
        // Establecer ganancia neta
        this.gananciaNeta = gananciaTotal;
        
        // Debug - imprimir totales finales
        System.out.println("Total Venta Final: " + this.total);
        System.out.println("Ganancia Total Final: " + this.gananciaNeta);
    }
}