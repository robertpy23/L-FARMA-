package com.App.Lfarma.service;

import com.App.Lfarma.entity.Usuario;
import com.App.Lfarma.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RedisTemplate<String, Usuario> redisTemplate;

    private static final String PREFIX = "usuario:";

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String redisKey = PREFIX + username;

        Usuario usuario = redisTemplate.opsForValue().get(redisKey);

        if (usuario == null) {
            usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            redisTemplate.opsForValue().set(redisKey, usuario);
        }

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .roles(usuario.getRol().toUpperCase())
                .build();
    }
}