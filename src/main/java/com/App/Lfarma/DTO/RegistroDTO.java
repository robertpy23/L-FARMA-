// RegistroDTO.java
package com.App.Lfarma.dto;

public class RegistroDTO {
    private String username; // puede ser email o nombre de usuario
    private String password;
    private String nombre;
    private String email;
    private String telefono;

    // getters y setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}

