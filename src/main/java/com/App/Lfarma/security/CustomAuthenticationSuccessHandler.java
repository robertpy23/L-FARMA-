package com.App.Lfarma.security;

import com.App.Lfarma.entity.Cliente;
import com.App.Lfarma.service.ClienteService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ClienteService clienteService;

    public CustomAuthenticationSuccessHandler(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        String username = authentication.getName();

        // Redirecciones por rol
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String rol = authority.getAuthority();

            if ("ROLE_ADMIN".equals(rol)) {
                response.sendRedirect("/dashboard_admin");
                return;

            } else if ("ROLE_EMPLEADO".equals(rol)) {
                response.sendRedirect("/dashboard_empleado");
                return;

            } else if ("ROLE_CLIENTE".equals(rol)) {
                // SOLO para clientes: verificar si existe, si no crearlo
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorUsername(username);
                if (clienteOpt.isEmpty()) {
                    Cliente nuevo = new Cliente();
                    nuevo.setUsername(username);
                    nuevo.setCodigo(username);
                    nuevo.setNombre(username);
                    clienteService.guardarCliente(nuevo);
                }
                response.sendRedirect("/vistaClientes");
                return;
            }
        }

        // Redirección por defecto si no se reconoce el rol
        response.sendRedirect("/");
    }
}