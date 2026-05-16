package com.cloud_technological.inversiones_prestar.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cloud_technological.inversiones_prestar.entity.UsuarioEntity;
import com.cloud_technological.inversiones_prestar.repositories.usuarios.UsuarioJPARepository;

import lombok.RequiredArgsConstructor;

/**
 * El esquema y los datos base (roles y usuario administrador) los crea el
 * script {@code docs/agents/recaudo_diario_schema.sql}. Ese script deja el
 * usuario admin con el password_hash de marcador {@code CAMBIAR_PASSWORD_HASH}.
 *
 * Esta clase detecta ese marcador y lo reemplaza por un hash BCrypt válido
 * para que el administrador pueda iniciar sesión. Sólo actúa una vez.
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String ADMIN_USUARIO = "admin";
    private static final String ADMIN_PASSWORD = "Admin123*";

    private final UsuarioJPARepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        usuarioRepository.findByUsuarioAndDeletedAtIsNull(ADMIN_USUARIO).ifPresentOrElse(
                this::asegurarPasswordValido,
                () -> log.warn("No existe el usuario '{}'. Ejecuta recaudo_diario_schema.sql primero.",
                        ADMIN_USUARIO));
    }

    private void asegurarPasswordValido(UsuarioEntity admin) {
        String hash = admin.getPasswordHash();
        boolean hashValido = hash != null && (hash.startsWith("$2a$")
                || hash.startsWith("$2b$") || hash.startsWith("$2y$"));

        if (hashValido) {
            return;
        }

        admin.setPasswordHash(passwordEncoder.encode(ADMIN_PASSWORD));
        usuarioRepository.save(admin);
        log.info("Password del administrador inicializado -> usuario: {} / email: {} / password: {}",
                admin.getUsuario(), admin.getEmail(), ADMIN_PASSWORD);
    }
}
