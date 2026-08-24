package com.sistema.bitacora.service;

import com.sistema.bitacora.dto.auth.AuthResponse;
import com.sistema.bitacora.dto.auth.LoginRequest;
import com.sistema.bitacora.dto.auth.RegisterRequest;
import com.sistema.bitacora.entity.Rol;
import com.sistema.bitacora.entity.Usuario;
import com.sistema.bitacora.repository.RolRepository;
import com.sistema.bitacora.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getCorreo(),
                        request.getPassword()
                )
        );

        var usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        var token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Rol rol = null;
        if (request.getRolId() != null) {
            rol = rolRepository.findById(request.getRolId())
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
        } else {
            rol = rolRepository.findByNombre("AGENTE")
                    .orElseThrow(() -> new RuntimeException("Rol AGENTE no encontrado"));
        }

        var usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())  // ← asignar apellido
                .correo(request.getCorreo())
                .password(passwordEncoder.encode(request.getPassword()))
                .rol(rol)
                .activo(true)
                .build();

        usuarioRepository.save(usuario);

        var token = jwtService.generateToken(usuario);

        return AuthResponse.builder()
                .id(usuario.getId())
                .token(token)
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getNombre())
                .build();
    }
}