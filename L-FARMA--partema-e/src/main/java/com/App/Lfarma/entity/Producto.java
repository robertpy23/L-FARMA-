package com.App.Lfarma.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PRODUCTOS")
public class Producto {

    @Id
    private String id;

    @Indexed(unique = true)
    private String codigo;

    @Indexed
    private String nombre;

    private double precio;
    private double costoCompra; // ✅ CAMPO AGREGADO
    private int cantidad;
    private String descripcion;
    private String presentacion;
    private String concentracion;
    private String lote;

    @Indexed
    private String categoria;

    private String principiosActivos;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Indexed
    private Date fechaVencimiento;

    private String proveedorId;

    @DBRef // ✅ RELACIÓN CORREGIDA
    private Proveedor proveedor;

    private String imagen;

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    // ✅ GETTER Y SETTER PARA COSTO COMPRA
    public double getCostoCompra() {
        return costoCompra;
    }

    public void setCostoCompra(double costoCompra) {
        this.costoCompra = costoCompra;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPresentacion() {
        return presentacion;
    }

    public void setPresentacion(String presentacion) {
        this.presentacion = presentacion;
    }

    public String getConcentracion() {
        return concentracion;
    }

    public void setConcentracion(String concentracion) {
        this.concentracion = concentracion;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getPrincipiosActivos() {
        return principiosActivos;
    }

    public void setPrincipiosActivos(String principiosActivos) {
        this.principiosActivos = principiosActivos;
    }

    public Date getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(Date fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    // ✅ GETTER Y SETTER PARA PROVEEDOR
    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return "Producto [id=" + id +
                ", codigo=" + codigo +
                ", nombre=" + nombre +
                ", descripcion=" + descripcion +
                ", presentacion=" + presentacion +
                ", concentracion=" + concentracion +
                ", lote=" + lote +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", costoCompra=" + costoCompra + // ✅ INCLUIDO EN toString
                ", categoria=" + categoria +
                ", principiosActivos=" + principiosActivos +
                ", fechaVencimiento=" + fechaVencimiento +
                ", proveedorId=" + proveedorId +
                ", proveedor=" + (proveedor != null ? proveedor.getNombre() : "null") + // ✅ INCLUIDO PROVEEDOR
                "]";
    }
}