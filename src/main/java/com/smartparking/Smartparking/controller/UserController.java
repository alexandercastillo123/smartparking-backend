package com.smartparking.Smartparking.controller;

import com.smartparking.Smartparking.dto.request.LoginRequestDto;
import com.smartparking.Smartparking.dto.request.RegistrationRequestDto;
import com.smartparking.Smartparking.dto.request.UserRequestDto;
import com.smartparking.Smartparking.dto.response.LoginResponseDto;
import com.smartparking.Smartparking.dto.response.UserResponseDto;
import com.smartparking.Smartparking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/v1/auth/users")
@Tag(name = "Auth & Users", description = "Endpoints para registro, login, logout y gestión de usuarios")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Crear usuario manualmente", description = "Permite a un administrador crear un usuario con correo, rol, estado y contraseña.")
    public ResponseEntity<UserResponseDto> createUser(@Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto response = userService.createUser(userRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/university-member")
    @Operation(summary = "Registrar miembro universitario", description = "Crea un usuario con rol 'university_member' y genera el perfil asociado.")
    public ResponseEntity<UserResponseDto> registerUniversityMember(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        UserResponseDto response = userService.registerUniversityMember(registrationRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/administrator")
    @Operation(summary = "Registrar administrador", description = "Alta de usuarios administradores para tareas operativas.")
    public ResponseEntity<UserResponseDto> registerAdministrator(@Valid @RequestBody RegistrationRequestDto registrationRequestDto) {
        UserResponseDto response = userService.registerAdministrator(registrationRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión", description = "Autentica credenciales y devuelve un JWT (token) más sessionId.")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = userService.login(loginRequestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cerrar sesión", description = "Marca la sesión como inactiva para cerrar la sesión del usuario.")
    public ResponseEntity<Void> logout(@PathVariable String sessionId) {
        userService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener usuario", description = "Obtiene los datos básicos del usuario por ID.")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable String userId) {
        UserResponseDto response = userService.findById(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Listar todos los usuarios", description = "Lista todos los usuarios registrados en el sistema.")
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Actualizar usuario", description = "Actualiza correo, contraseña (si se envía), rol y estado.")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable String userId, @Valid @RequestBody UserRequestDto userRequestDto) {
        UserResponseDto response = userService.updateUser(userId, userRequestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Eliminar usuario", description = "Elimina definitivamente al usuario por ID.")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
