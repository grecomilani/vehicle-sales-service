package com.grecomilani.vehiclesales.infrastructure.web.internal;

import com.grecomilani.vehiclesales.application.usecase.SyncVehicleResult;
import com.grecomilani.vehiclesales.application.usecase.SyncVehicleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints de uso interno, chamados apenas pelo {@code vehicle-platform-api}.
 */
@RestController
@RequestMapping("/internal/vehicles")
public class InternalVehicleController {

    private final SyncVehicleUseCase syncVehicleUseCase;

    public InternalVehicleController(SyncVehicleUseCase syncVehicleUseCase) {
        this.syncVehicleUseCase = syncVehicleUseCase;
    }

    /**
     * Cria ou atualiza o veículo. Responde 201 quando o veículo é criado e
     * 200 quando um veículo existente é atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehicleResponse> sync(@PathVariable UUID id,
                                                @Valid @RequestBody VehicleSyncRequest request) {
        SyncVehicleResult result = syncVehicleUseCase.execute(request.toCommand(id));
        HttpStatus status = result.created() ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(VehicleResponse.from(result.vehicle()));
    }
}
