package com.sesesp.almacen.auth;

import com.sesesp.almacen.auth.dto.LoginRequestDto;
import com.sesesp.almacen.auth.dto.LoginResponseDto;
import com.sesesp.almacen.domain.entity.UsuarioEntity;
import com.sesesp.almacen.domain.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        UsuarioEntity usuario = usuarioRepository.findByUsername(request.username())
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElse(null);

        if (usuario == null || !passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            return ResponseEntity.status(401)
                    .body(Map.of("mensaje", "Credenciales incorrectas"));
        }

        String token = jwtUtil.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDto(
                token,
                usuario.getRol().name(),
                usuario.getNombre(),
                usuario.getIdUsuario()
        ));
    }
}
