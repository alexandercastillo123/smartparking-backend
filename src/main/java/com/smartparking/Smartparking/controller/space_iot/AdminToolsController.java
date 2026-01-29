package com.smartparking.Smartparking.controller.space_iot;

import com.smartparking.Smartparking.entity.space_iot.ParkingSpace;
import com.smartparking.Smartparking.repository.reservation.ReservationRepository;
import com.smartparking.Smartparking.repository.space_iot.ParkingSpaceRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Tools", description = "Herramientas administrativas para gestión del sistema")
public class AdminToolsController {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ReservationRepository reservationRepository;

    @PostMapping("/reset-parking-spaces")
    @PreAuthorize("isAuthenticated()") // Allow any logged-in user for testing
    @Transactional
    @Operation(summary = "Resetear espacios de estacionamiento", 
               description = "Elimina todos los espacios y crea 10 nuevos con códigos cortos (A1-A10)")
    public ResponseEntity<Map<String, Object>> resetParkingSpaces() {
        
        // Paso 1: Eliminar todas las reservas (para evitar conflictos de foreign key)
        long deletedReservations = reservationRepository.count();
        reservationRepository.deleteAll();
        
        // Paso 2: Eliminar todos los espacios existentes
        long deletedSpaces = parkingSpaceRepository.count();
        parkingSpaceRepository.deleteAll();
        
        // Paso 3: Crear 10 espacios nuevos con códigos cortos
        List<ParkingSpace> newSpaces = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            ParkingSpace space = new ParkingSpace();
            space.setCode("A" + i);
            space.setStatus(ParkingSpace.SpaceStatus.available);
            space.setCreatedAt(LocalDateTime.now());
            space.setLastUpdated(LocalDateTime.now());
            newSpaces.add(space);
        }
        
        parkingSpaceRepository.saveAll(newSpaces);
        
        // Verificar
        List<String> codes = parkingSpaceRepository.findAll()
                .stream()
                .map(ParkingSpace::getCode)
                .sorted()
                .toList();
        
        return ResponseEntity.ok(Map.of(
            "message", "Espacios de estacionamiento reseteados exitosamente",
            "deletedReservations", deletedReservations,
            "deletedSpaces", deletedSpaces,
            "newSpaces", 10,
            "spaceCodes", codes
        ));
    }
}
