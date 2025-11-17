package com.App.Lfarma.service;

import com.App.Lfarma.entity.Usuario;
import com.App.Lfarma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RedisTemplate<String, Usuario> redisTemplate;

    private static final String PREFIX = "usuario:";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Buscando usuario: " + username);

        String redisKey = PREFIX + username;
        Usuario usuario = redisTemplate.opsForValue().get(redisKey);

        if (usuario == null) {
            System.out.println("📦 Usuario no encontrado en Redis, buscando en BD...");
            usuario = usuarioRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        System.out.println("❌ Usuario no encontrado en BD: " + username);
                        return new UsernameNotFoundException("Usuario no encontrado: " + username);
                    });
            redisTemplate.opsForValue().set(redisKey, usuario);
            System.out.println("✅ Usuario encontrado en BD y guardado en Redis");
        } else {
            System.out.println("✅ Usuario encontrado en Redis");
        }

        // Log del rol encontrado
        System.out.println("🎭 Rol encontrado en BD/Redis: '" + usuario.getRol() + "'");

        UserDetails userDetails = User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().toUpperCase()) // Esto crea ROLE_ADMIN, ROLE_EMPLEADO, etc.
                .build();

        System.out.println("🏷️  Authorities finales: " + userDetails.getAuthorities());

        return userDetails;
    }
}