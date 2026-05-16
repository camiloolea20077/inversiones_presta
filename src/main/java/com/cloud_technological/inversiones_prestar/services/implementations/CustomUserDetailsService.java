package com.cloud_technological.inversiones_prestar.services.implementations;

import java.util.Collections;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.entity.UsuarioEntity;
import com.cloud_technological.inversiones_prestar.repositories.usuarios.UsuarioJPARepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioJPARepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        UsuarioEntity usuario = usuarioRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UsernameNotFoundException("El usuario está inactivo");
        }

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(usuario.getRol().getNombre()));

        return new User(usuario.getEmail(), usuario.getPasswordHash(), authorities);
    }
}
