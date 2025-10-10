package com.App.Lfarma.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.repository.ClienteRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obtenerClientePorCodigo(String codigo) {
        return clienteRepository.findByCodigo(codigo);
    }

    public Optional<Cliente> obtenerClientePorId(String id) {
        return clienteRepository.findById(id);
    }

    public Cliente guardarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public Cliente agregarCliente(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(String codigo) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCodigo(codigo);
        if (clienteOpt.isPresent()) {
            clienteRepository.deleteById(clienteOpt.get().getId());
        } else {
            throw new RuntimeException("No se encontró cliente con el código: " + codigo);
        }
    }
    // ClienteService.java (añadir método buscarPorEmail)
    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
    // ClienteService.java
    public Optional<Cliente> obtenerClientePorUsername(String username) {
        return clienteRepository.findByUsername(username);
    }


}