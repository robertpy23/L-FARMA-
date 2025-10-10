// RegistroService.java
package com.App.Lfarma.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.App.Lfarma.dto.RegistroDTO;
import com.App.Lfarma.entity.Usuario;
import com.App.Lfarma.entity.Cliente;

@Service
public class RegistroService {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    public Cliente registrarUsuarioYCliente(RegistroDTO dto) {
        // 1) validaciones simples
        if (usuarioService.existeUsuario(dto.getUsername())) {
            throw new RuntimeException("El username ya existe");
        }
        if (clienteService.buscarPorEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("El email ya está registrado");
        }

        // 2) crear usuario MySQL
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(dto.getPassword()); // UsuarioService.se encargará de encriptar
        usuario.setRol("CLIENTE"); // rol simple; Security usa hasRole("CLIENTE")
        Usuario savedUsuario = usuarioService.registrar(usuario);

        try {
            // 3) crear cliente Mongo y vincular username (NO password)
            Cliente cliente = new Cliente();
            cliente.setNombre(dto.getNombre());
            cliente.setEmail(dto.getEmail());
            cliente.setTelefono(dto.getTelefono());
            cliente.setUsername(savedUsuario.getUsername());
            // generar código cliente si quieres (o usa servicio)
            cliente.setCodigo("C-" + java.util.UUID.randomUUID().toString().substring(0,8));
            Cliente savedCliente = clienteService.guardarCliente(cliente);

            return savedCliente;
        } catch (Exception e) {
            // compensación: eliminar usuario creado en MySQL
            try {
                usuarioService.eliminarUsuario(savedUsuario.getId());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            throw new RuntimeException("Error creando cliente en Mongo. Usuario revertido. " + e.getMessage(), e);
        }
    }
}
