// CustomAuthenticationSuccessHandler.java - VERSIÓN CORREGIDA
package com.App.Lfarma.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/login?error=true"; // Por defecto

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();
            System.out.println("🔍 Rol detectado: " + role); // Debug

            if (role.equals("ROLE_ADMIN")) {
                redirectUrl = "/dashboard_admin";
                break;
            } else if (role.equals("ROLE_EMPLEADO")) {
                redirectUrl = "/dashboard_empleado";
                break;
            } else if (role.equals("ROLE_CLIENTE")) {
                redirectUrl = "/vistaClientes";
                break;
            }
        }

        System.out.println("🎯 Redirigiendo a: " + redirectUrl);
        response.sendRedirect(redirectUrl);
    }
}