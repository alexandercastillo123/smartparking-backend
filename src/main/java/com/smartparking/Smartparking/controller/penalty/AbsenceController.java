package com.smartparking.Smartparking.controller.penalty;

import com.smartparking.Smartparking.dto.response.penalty.AbsenceCounterResponse;
import com.smartparking.Smartparking.entity.penalty.AbsenceCounter;
import com.smartparking.Smartparking.entity.penalty.Suspension;
import com.smartparking.Smartparking.repository.penalty.AbsenceCounterRepository;
import com.smartparking.Smartparking.repository.penalty.SuspensionRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/absence")
@RequiredArgsConstructor
@Tag(name = "Absence Management", description = "Gestión de ausencias y penalidades")
public class AbsenceController {

    private final AbsenceCounterRepository absenceCounterRepository;
    private final SuspensionRepository suspensionRepository;

    @GetMapping("/counter")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener contador de ausencias", description = "Retorna el contador de ausencias del usuario autenticado con su estado actual")
    public ResponseEntity<AbsenceCounterResponse> getAbsenceCounter() {
        String userId = getCurrentUserId();

        // Buscar contador o crear uno nuevo
        AbsenceCounter counter = absenceCounterRepository.findByUserId(userId)
                .orElseGet(() -> {
                    AbsenceCounter newCounter = new AbsenceCounter();
                    newCounter.setUserId(userId);
                    newCounter.setAbsenceCount(0);
                    newCounter.setStrikeCount(0);
                    newCounter.setMaxStrikes(3);
                    newCounter.setLastUpdated(LocalDateTime.now());
                    return newCounter;
                });

        // Verificar si está suspendido
        Optional<Suspension> activeSuspension = suspensionRepository
                .findByUserIdAndStatus(userId, Suspension.Status.active)
                .stream()
                .filter(s -> s.getEndDate().isAfter(LocalDateTime.now()))
                .findFirst();

        // Determinar estado y mensaje
        String status;
        String message;

        if (activeSuspension.isPresent()) {
            status = "suspended";
            message = "Cuenta suspendida hasta " + activeSuspension.get().getEndDate().toLocalDate();
        } else if (counter.getStrikeCount() >= counter.getMaxStrikes()) {
            status = "danger";
            message = "Has alcanzado el máximo de ausencias. Próxima ausencia resultará en suspensión.";
        } else if (counter.getStrikeCount() == 2) {
            status = "danger";
            message = "¡Cuidado! Una ausencia más y tu cuenta será suspendida por 1 semana.";
        } else if (counter.getStrikeCount() == 1) {
            status = "warning";
            message = "Tienes 1 ausencia registrada. Evita más ausencias para no ser penalizado.";
        } else {
            status = "safe";
            message = "Sin ausencias. ¡Buen trabajo!";
        }

        AbsenceCounterResponse response = AbsenceCounterResponse.builder()
                .userId(userId)
                .absenceCount(counter.getAbsenceCount())
                .strikeCount(counter.getStrikeCount())
                .maxStrikes(counter.getMaxStrikes())
                .lastUpdated(counter.getLastUpdated())
                .status(status)
                .message(message)
                .build();

        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof String userId) {
            return userId;
        }
        throw new IllegalStateException("User not authenticated");
    }
}
