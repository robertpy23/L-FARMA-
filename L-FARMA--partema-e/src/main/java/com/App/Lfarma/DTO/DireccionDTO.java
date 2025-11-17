package com.App.Lfarma.DTO;

public class DireccionDTO {
    private String username;
    private String direccionTexto;
    private Double latitud;
    private Double longitud;

    // Getters y Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getDireccionTexto() { return direccionTexto; }
    public void setDireccionTexto(String direccionTexto) { this.direccionTexto = direccionTexto; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}