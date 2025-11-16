package com.App.Lfarma.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PRODUCTOS")
public class Producto {

    @Id
    private String id;

    @Indexed(unique = true)
    @NotBlank(message = "El código del producto es obligatorio")
    @Size(min = 3, max = 20, message = "El código debe tener entre 3 y 20 caracteres")
    private String codigo;

    @Indexed
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotNull(message = "El costo de compra es obligatorio")
    @PositiveOrZero(message = "El costo de compra no puede ser negativo")
    private double costoCompra;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private double precio;

    @NotNull(message = "La cantidad es obligatoria")
    @PositiveOrZero(message = "La cantidad no puede ser negativa")
    private int cantidad;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @Size(max = 50, message = "La presentación no puede exceder 50 caracteres")
    private String presentacion;

    @Size(max = 50, message = "La concentración no puede exceder 50 caracteres")
    private String concentracion;

    @Size(max = 20, message = "El lote no puede exceder 20 caracteres")
    private String lote;

    @Indexed
    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 50, message = "La categoría no puede exceder 50 caracteres")
    private String categoria;

    @Size(max = 200, message = "Los principios activos no pueden exceder 200 caracteres")
    private String principiosActivos;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Indexed
    private Date fechaVencimiento;

    @Size(max = 50, message = "El ID del proveedor no puede exceder 50 caracteres")
    private String proveedorId;

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

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public double getCostoCompra() {
        return costoCompra;
    }

    public void setCostoCompra(double costoCompra) {
        this.costoCompra = costoCompra;
    }

    @Override
    public String toString() {
        return "Producto [id=" + id
                + ", codigo=" + codigo
                + ", nombre=" + nombre
                + ", descripcion=" + descripcion
                + ", presentacion=" + presentacion
                + ", concentracion=" + concentracion
                + ", lote=" + lote
                + ", cantidad=" + cantidad
                + ", precio=" + precio
                + ", costoCompra=" + costoCompra
                + ", categoria=" + categoria
                + ", principiosActivos=" + principiosActivos
                + ", fechaVencimiento=" + fechaVencimiento
                + ", proveedorId=" + proveedorId
                + "]";
    }
}