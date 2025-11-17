package com.App.Lfarma.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import java.util.Date;
import java.util.List;

@Document(collection = "suministros")
public class Suministro {

    @Id
    private String id;

    @DBRef
    private Proveedor proveedor;

    private Date fechaSuministro;
    private String numeroFactura;
    private String observaciones;
    private String estado; // PENDIENTE, RECIBIDO, CANCELADO

    private List<DetalleSuministro> detalles;

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }

    public Date getFechaSuministro() { return fechaSuministro; }
    public void setFechaSuministro(Date fechaSuministro) { this.fechaSuministro = fechaSuministro; }

    public String getNumeroFactura() { return numeroFactura; }
    public void setNumeroFactura(String numeroFactura) { this.numeroFactura = numeroFactura; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public List<DetalleSuministro> getDetalles() { return detalles; }
    public void setDetalles(List<DetalleSuministro> detalles) { this.detalles = detalles; }

    // Calcular total del suministro
    public double getTotalSuministro() {
        if (detalles == null) return 0;
        return detalles.stream()
                .mapToDouble(detalle -> detalle.getCantidad() * detalle.getPrecioCompra())
                .sum();
    }
}