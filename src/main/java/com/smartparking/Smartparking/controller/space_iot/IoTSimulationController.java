package com.smartparking.Smartparking.controller.space_iot;

import com.smartparking.Smartparking.entity.reservation.Reservation;
import com.smartparking.Smartparking.entity.space_iot.ParkingSpace;
import com.smartparking.Smartparking.repository.reservation.ReservationRepository;
import com.smartparking.Smartparking.repository.space_iot.ParkingSpaceRepository;
import com.smartparking.Smartparking.service.reservation.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/iot")
@RequiredArgsConstructor
@Tag(name = "IoT Simulation", description = "Endpoints para simular eventos IoT sin hardware físico")
public class IoTSimulationController {

    private final ParkingSpaceRepository parkingSpaceRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    @PostMapping("/simulate-arrival/{spaceCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simular llegada de vehículo", description = "Simula que un vehículo llegó al espacio reservado, activando la reserva")
    public ResponseEntity<Map<String, Object>> simulateArrival(@PathVariable String spaceCode) {
        
        // Buscar espacio
        ParkingSpace space = parkingSpaceRepository.findByCode(spaceCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espacio no encontrado"));

        // Verificar que tenga una reserva
        if (space.getCurrentReservationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este espacio no tiene una reserva activa");
        }

        // Buscar la reserva
        Reservation reservation = reservationRepository.findById(space.getCurrentReservationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        // Verificar que esté en estado confirmed o pending
        if (reservation.getStatus() != Reservation.ReservationStatus.confirmed && 
            reservation.getStatus() != Reservation.ReservationStatus.pending) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "La reserva debe estar en estado 'confirmed' o 'pending' para activarse. Estado actual: " + reservation.getStatus());
        }

        // REMOVED TIME RESTRICTION - Allow simulation at any time for testing
        // Activar la reserva
        reservation.setStatus(Reservation.ReservationStatus.active);
        reservation.setStartTime(LocalDateTime.now()); // Actualizar hora de inicio real
        reservationRepository.save(reservation);

        // Actualizar estado del espacio
        space.setStatus(ParkingSpace.SpaceStatus.occupied);
        space.setLastUpdated(LocalDateTime.now());
        parkingSpaceRepository.save(space);

        return ResponseEntity.ok(Map.of(
            "message", "Llegada simulada exitosamente",
            "spaceCode", spaceCode,
            "reservationId", reservation.getReservationId(),
            "status", "occupied",
            "startTime", reservation.getStartTime()
        ));
    }

    @PostMapping("/simulate-departure/{spaceCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simular salida de vehículo", description = "Simula que un vehículo salió del espacio, completando la reserva")
    public ResponseEntity<Map<String, Object>> simulateDeparture(@PathVariable String spaceCode) {
        
        // Buscar espacio
        ParkingSpace space = parkingSpaceRepository.findByCode(spaceCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espacio no encontrado"));

        // Verificar que esté ocupado
        if (space.getStatus() != ParkingSpace.SpaceStatus.occupied) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Este espacio no está ocupado");
        }

        // Buscar la reserva activa
        if (space.getCurrentReservationId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay reserva activa en este espacio");
        }

        Reservation reservation = reservationRepository.findById(space.getCurrentReservationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        // Verificar que esté activa
        if (reservation.getStatus() != Reservation.ReservationStatus.active) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La reserva no está activa");
        }

        // Completar la reserva
        reservation.setStatus(Reservation.ReservationStatus.completed);
        reservation.setCompletedAt(LocalDateTime.now());
        reservation.setEndTime(LocalDateTime.now());

        // Calcular costo basado en tiempo de uso
        long minutes = Duration.between(reservation.getStartTime(), LocalDateTime.now()).toMinutes();
        BigDecimal cost = BigDecimal.valueOf(minutes).multiply(new BigDecimal("0.05"));
        reservation.setTotalCost(cost);
        reservation.setPaymentStatus(Reservation.PaymentStatus.paid);

        reservationRepository.save(reservation);

        // Liberar espacio
        space.setStatus(ParkingSpace.SpaceStatus.available);
        space.setCurrentReservationId(null);
        space.setLastUpdated(LocalDateTime.now());
        parkingSpaceRepository.save(space);

        return ResponseEntity.ok(Map.of(
            "message", "Salida simulada exitosamente",
            "spaceCode", spaceCode,
            "reservationId", reservation.getReservationId(),
            "status", "available",
            "duration", minutes + " minutos",
            "totalCost", cost
        ));
    }

    @PostMapping("/simulate-absence/{reservationId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Simular ausencia", description = "Simula que el usuario no llegó a tiempo, expirando la reserva y registrando ausencia")
    public ResponseEntity<Map<String, Object>> simulateAbsence(@PathVariable String reservationId) {
        
        // Buscar la reserva
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva no encontrada"));

        // Verificar que esté en estado que permita expirar
        if (reservation.getStatus() != Reservation.ReservationStatus.pending && 
            reservation.getStatus() != Reservation.ReservationStatus.confirmed) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Solo se pueden expirar reservas en estado 'pending' o 'confirmed'");
        }

        // Usar el servicio para expirar (esto registrará la ausencia y aplicará penalidades)
        reservationService.expireReservationManually(reservationId);

        return ResponseEntity.ok(Map.of(
            "message", "Ausencia simulada exitosamente",
            "reservationId", reservationId,
            "status", "expired",
            "note", "Se ha registrado una ausencia y se aplicaron las penalidades correspondientes"
        ));
    }

    @GetMapping("/space-status/{spaceCode}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver estado del espacio", description = "Obtiene el estado actual de un espacio para debugging")
    public ResponseEntity<Map<String, Object>> getSpaceStatus(@PathVariable String spaceCode) {
        
        ParkingSpace space = parkingSpaceRepository.findByCode(spaceCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espacio no encontrado"));

        Map<String, Object> response = Map.of(
            "spaceCode", space.getCode(),
            "status", space.getStatus().name(),
            "currentReservationId", space.getCurrentReservationId() != null ? space.getCurrentReservationId() : "ninguna",
            "lastUpdated", space.getLastUpdated() != null ? space.getLastUpdated() : "nunca"
        );

        return ResponseEntity.ok(response);
    }
}
