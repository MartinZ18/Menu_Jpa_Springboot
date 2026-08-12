package com.menujpa.controllers;

import com.menujpa.dto.AuthResponse;
import com.menujpa.dto.CambiarContraseniaRequest;
import com.menujpa.dto.LoginRequest;
import com.menujpa.dto.RegistroRequest;
import com.menujpa.entities.Cliente;
import com.menujpa.security.JwtService;
import com.menujpa.security.MultiRoleUserDetailsService;
import com.menujpa.services.ClienteServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/auth")
@Tag(name = "Autenticación", description = "Registro de clientes y login para Cliente, Chef, Mesero y Gerente")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ClienteServiceImpl clienteService;
    private final MultiRoleUserDetailsService multiRoleUserDetailsService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService,
                           ClienteServiceImpl clienteService, MultiRoleUserDetailsService multiRoleUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.clienteService = clienteService;
        this.multiRoleUserDetailsService = multiRoleUserDetailsService;
    }

    @Operation(summary = "Registrar un nuevo cliente")
    @PostMapping("/registrarse")
    public ResponseEntity<?> registrarse(@Valid @RequestBody RegistroRequest datos, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errores = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errores));
        }
        // Chequeo cruzado a las 4 tablas de rol: "usuario" no tiene un UNIQUE global en la
        // base (cada rol es una tabla separada), asi que sin esto un Cliente podria registrarse
        // con el mismo usuario que ya tiene un Chef/Mesero/Gerente.
        if (multiRoleUserDetailsService.existeUsuario(datos.getUsuario())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "El usuario \"" + datos.getUsuario() + "\" ya está en uso."));
        }
        try {
            Cliente cliente = new Cliente();
            cliente.setNombre(datos.getNombre());
            cliente.setApellido(datos.getApellido());
            cliente.setCedula(datos.getCedula());
            cliente.setTelefono(datos.getTelefono());
            cliente.setCorreo(datos.getCorreo());
            cliente.setUsuario(datos.getUsuario());
            cliente.setContrasenia(datos.getContrasenia());
            clienteService.save(cliente);

            String token = jwtService.generarToken(datos.getUsuario(), "CLIENTE");
            return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token, "CLIENTE"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Iniciar sesión (Cliente, Chef, Mesero o Gerente)")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest datos, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Usuario y contraseña son obligatorios"));
        }
        try {
            Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(datos.getUsuario(), datos.getContrasenia()));
            String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .orElseThrow(() -> new IllegalStateException("El usuario no tiene un rol asignado"));
            String token = jwtService.generarToken(datos.getUsuario(), rol);
            return ResponseEntity.ok(new AuthResponse(token, rol));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Usuario o contraseña incorrectos"));
        }
    }

    @Operation(summary = "Cambiar la propia contraseña (requiere estar autenticado y saber la actual)")
    @PutMapping("/contrasenia")
    public ResponseEntity<?> cambiarContrasenia(@Valid @RequestBody CambiarContraseniaRequest datos,
                                                 BindingResult bindingResult, Authentication authentication) {
        if (bindingResult.hasErrors()) {
            String errores = bindingResult.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", errores));
        }
        try {
            multiRoleUserDetailsService.cambiarContrasenia(
                authentication.getName(), datos.getContraseniaActual(), datos.getContraseniaNueva());
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada correctamente."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
