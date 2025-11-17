package com.App.Lfarma.security;

import com.App.Lfarma.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationSuccessHandler successHandler;
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(AuthenticationSuccessHandler successHandler,
                          UserDetailsServiceImpl userDetailsService) {
        this.successHandler = successHandler;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Recursos estáticos
                        .requestMatchers("/styles.css", "/css/", "/js/", "/images/",
                                "/f.jpg/", "/webjars/", "/favicon.ico", "/f5.jpg",
                                "/estiloprincipal.css", "/stylesvisualizarproductos.css").permitAll()
                        // Páginas públicas
                        .requestMatchers("/login", "/register", "/register-admin",
                                "/register-empleado", "/auth/register").permitAll()

                        // ==================== RUTAS EXCLUSIVAS PARA ADMIN ====================
                        .requestMatchers(
                                "/dashboard_admin",
                                "/predicciones/",
                                "/predicciones/dashboard/",
                                "/productos/registrar-productos",
                                "/productos/actualizar-productos",
                                "/productos/actualizar",
                                "/productos/eliminar",
                                "/productos/{id}/imagen",
                                "/clientes/eliminar",
                                "/clientes/actualizar",
                                "/clientes/editar/",
                                "/proveedores/",           // ✅ NUEVO: Módulo proveedores
                                "/suministros/"            // ✅ NUEVO: Módulo suministros
                        ).hasRole("ADMIN")

                        // ==================== RUTAS EXCLUSIVAS PARA EMPLEADO ====================
                        .requestMatchers("/dashboard_empleado").hasRole("EMPLEADO")

                        // ==================== RUTAS PARA CLIENTE ====================
                        .requestMatchers("/vistaClientes", "/carrito/").hasRole("CLIENTE")

                        // ==================== RUTAS COMPARTIDAS ADMIN/EMPLEADO ====================

                        // ✅ PRODUCTOS - Admin: gestionar completo, Empleado: solo visualizar
                        .requestMatchers(
                                "/productos",
                                "/productos/buscar-productos",
                                "/productos/buscar",
                                "/productos/images/"
                        ).hasAnyRole("ADMIN", "EMPLEADO")

                        // ✅ CLIENTES - Admin: gestionar completo, Empleado: solo agregar/ver
                        .requestMatchers(
                                "/clientes",
                                "/clientes/agregar",
                                "/clientes/api/"
                        ).hasAnyRole("ADMIN", "EMPLEADO")

                        // ✅ FACTURAS - Ambos pueden gestionar
                        .requestMatchers(
                                "/facturas",
                                "/facturas/"
                        ).hasAnyRole("ADMIN", "EMPLEADO")

                        // Cualquier otra ruta requiere autenticación
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .successHandler(successHandler) // ✅ Usa el handler corregido
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}