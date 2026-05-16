package com.cloud_technological.inversiones_prestar.services.implementations;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorComboDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorListDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorRequestDto;
import com.cloud_technological.inversiones_prestar.dto.trabajadores.TrabajadorResponseDto;
import com.cloud_technological.inversiones_prestar.entity.RolEntity;
import com.cloud_technological.inversiones_prestar.entity.TrabajadorEntity;
import com.cloud_technological.inversiones_prestar.entity.UsuarioEntity;
import com.cloud_technological.inversiones_prestar.repositories.roles.RolJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorJPARepository;
import com.cloud_technological.inversiones_prestar.repositories.trabajadores.TrabajadorQueryRepository;
import com.cloud_technological.inversiones_prestar.repositories.usuarios.UsuarioJPARepository;
import com.cloud_technological.inversiones_prestar.services.TrabajadorService;
import com.cloud_technological.inversiones_prestar.utils.GlobalException;
import com.cloud_technological.inversiones_prestar.utils.PageableDto;
import com.cloud_technological.inversiones_prestar.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrabajadorServiceImpl implements TrabajadorService {

    private static final String ROL_TRABAJADOR = "TRABAJADOR";

    private final TrabajadorJPARepository trabajadorRepository;
    private final TrabajadorQueryRepository trabajadorQueryRepository;
    private final UsuarioJPARepository usuarioRepository;
    private final RolJPARepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional
    public TrabajadorResponseDto crear(TrabajadorRequestDto dto) {
        String documento = dto.getDocumento().trim();

        trabajadorRepository.findByDocumentoAndDeletedAtIsNull(documento).ifPresent(t -> {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe un trabajador activo con el documento " + documento);
        });

        String nombre = dto.getNombre().trim();
        Long usuarioId = dto.getUsuarioId();

        if (quiereAcceso(dto)) {
            usuarioId = crearUsuarioParaTrabajador(dto, nombre);
        } else {
            validarUsuarioExistente(usuarioId);
        }

        TrabajadorEntity entity = TrabajadorEntity.builder()
                .documento(documento)
                .nombre(nombre)
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .usuarioId(usuarioId)
                .activo(dto.getActivo() == null ? Boolean.TRUE : dto.getActivo())
                .updatedBy(securityUtils.getUsuarioId())
                .build();

        return toResponse(trabajadorRepository.save(entity));
    }

    @Override
    @Transactional
    public TrabajadorResponseDto actualizar(Long id, TrabajadorRequestDto dto) {
        TrabajadorEntity entity = buscar(id);
        String documento = dto.getDocumento().trim();

        trabajadorRepository.findByDocumentoAndDeletedAtIsNull(documento)
                .filter(otro -> !otro.getId().equals(id))
                .ifPresent(otro -> {
                    throw new GlobalException(HttpStatus.CONFLICT,
                            "Ya existe otro trabajador con el documento " + documento);
                });

        String nombre = dto.getNombre().trim();

        if (entity.getUsuarioId() != null) {
            actualizarUsuario(entity.getUsuarioId(), dto, nombre);
        } else if (quiereAcceso(dto)) {
            entity.setUsuarioId(crearUsuarioParaTrabajador(dto, nombre));
        }

        entity.setDocumento(documento);
        entity.setNombre(nombre);
        entity.setTelefono(dto.getTelefono());
        entity.setDireccion(dto.getDireccion());
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(securityUtils.getUsuarioId());

        return toResponse(trabajadorRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public TrabajadorResponseDto obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrabajadorListDto> listar(PageableDto<Object> pageable) {
        return trabajadorQueryRepository.listar(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrabajadorComboDto> listarActivos() {
        return trabajadorRepository.findByActivoTrueAndDeletedAtIsNullOrderByNombreAsc().stream()
                .map(t -> TrabajadorComboDto.builder()
                        .id(t.getId())
                        .nombre(t.getNombre())
                        .documento(t.getDocumento())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public TrabajadorResponseDto cambiarEstado(Long id, Boolean activo) {
        TrabajadorEntity entity = buscar(id);
        entity.setActivo(activo);
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setUpdatedBy(securityUtils.getUsuarioId());
        return toResponse(trabajadorRepository.save(entity));
    }

    /* ===================== Acceso al sistema ===================== */

    /** El admin diligenció algún dato de acceso (correo/usuario/contraseña). */
    private boolean quiereAcceso(TrabajadorRequestDto dto) {
        return notBlank(dto.getUsuario()) || notBlank(dto.getPassword()) || notBlank(dto.getCorreo());
    }

    private Long crearUsuarioParaTrabajador(TrabajadorRequestDto dto, String nombre) {
        if (!notBlank(dto.getUsuario()) || !notBlank(dto.getPassword())) {
            throw new GlobalException(HttpStatus.BAD_REQUEST,
                    "Para dar acceso al sistema, el usuario y la contraseña son obligatorios");
        }

        String usuario = dto.getUsuario().trim();
        usuarioRepository.findByUsuarioAndDeletedAtIsNull(usuario).ifPresent(u -> {
            throw new GlobalException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con el nombre de usuario " + usuario);
        });

        String correo = notBlank(dto.getCorreo()) ? dto.getCorreo().trim() : null;
        if (correo != null) {
            usuarioRepository.findByEmailAndDeletedAtIsNull(correo).ifPresent(u -> {
                throw new GlobalException(HttpStatus.CONFLICT,
                        "Ya existe un usuario con el correo " + correo);
            });
        }

        RolEntity rol = rolRepository.findByNombre(ROL_TRABAJADOR)
                .orElseThrow(() -> new GlobalException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "No existe el rol TRABAJADOR"));

        UsuarioEntity nuevo = UsuarioEntity.builder()
                .rol(rol)
                .nombre(nombre)
                .usuario(usuario)
                .email(correo)
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .activo(Boolean.TRUE)
                .updatedBy(securityUtils.getUsuarioId())
                .build();

        return usuarioRepository.save(nuevo).getId();
    }

    private void actualizarUsuario(Long usuarioId, TrabajadorRequestDto dto, String nombre) {
        UsuarioEntity usuario = usuarioRepository.findById(usuarioId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "El usuario de acceso del trabajador no existe"));

        usuario.setNombre(nombre);

        if (notBlank(dto.getUsuario())) {
            String nuevoUsuario = dto.getUsuario().trim();
            usuarioRepository.findByUsuarioAndDeletedAtIsNull(nuevoUsuario)
                    .filter(otro -> !otro.getId().equals(usuarioId))
                    .ifPresent(otro -> {
                        throw new GlobalException(HttpStatus.CONFLICT,
                                "Ya existe un usuario con el nombre de usuario " + nuevoUsuario);
                    });
            usuario.setUsuario(nuevoUsuario);
        }

        if (notBlank(dto.getCorreo())) {
            String nuevoCorreo = dto.getCorreo().trim();
            usuarioRepository.findByEmailAndDeletedAtIsNull(nuevoCorreo)
                    .filter(otro -> !otro.getId().equals(usuarioId))
                    .ifPresent(otro -> {
                        throw new GlobalException(HttpStatus.CONFLICT,
                                "Ya existe un usuario con el correo " + nuevoCorreo);
                    });
            usuario.setEmail(nuevoCorreo);
        }

        if (notBlank(dto.getPassword())) {
            usuario.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }

        usuario.setUpdatedAt(LocalDateTime.now());
        usuario.setUpdatedBy(securityUtils.getUsuarioId());
        usuarioRepository.save(usuario);
    }

    /* ===================== Helpers ===================== */

    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private TrabajadorEntity buscar(Long id) {
        return trabajadorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new GlobalException(HttpStatus.NOT_FOUND,
                        "Trabajador no encontrado"));
    }

    private void validarUsuarioExistente(Long usuarioId) {
        if (usuarioId == null) {
            return;
        }
        usuarioRepository.findById(usuarioId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new GlobalException(HttpStatus.BAD_REQUEST,
                        "El usuario asociado no existe"));
    }

    private TrabajadorResponseDto toResponse(TrabajadorEntity entity) {
        String usuarioNombre = null;
        String correo = null;
        String usuario = null;
        if (entity.getUsuarioId() != null) {
            UsuarioEntity u = usuarioRepository.findById(entity.getUsuarioId()).orElse(null);
            if (u != null) {
                usuarioNombre = u.getNombre();
                correo = u.getEmail();
                usuario = u.getUsuario();
            }
        }
        return TrabajadorResponseDto.builder()
                .id(entity.getId())
                .documento(entity.getDocumento())
                .nombre(entity.getNombre())
                .telefono(entity.getTelefono())
                .direccion(entity.getDireccion())
                .activo(entity.getActivo())
                .usuarioId(entity.getUsuarioId())
                .usuarioNombre(usuarioNombre)
                .correo(correo)
                .usuario(usuario)
                .build();
    }
}
