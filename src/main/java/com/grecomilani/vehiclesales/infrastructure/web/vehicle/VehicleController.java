package com.grecomilani.vehiclesales.infrastructure.web.vehicle;

import com.grecomilani.vehiclesales.application.usecase.ListAvailableVehiclesUseCase;
import com.grecomilani.vehiclesales.infrastructure.web.PageResponse;
import com.grecomilani.vehiclesales.infrastructure.web.VehicleResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de consulta de veículos, consumidos pelo frontend.
 */
@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    static final int MAX_PAGE_SIZE = 100;

    private final ListAvailableVehiclesUseCase listAvailableVehiclesUseCase;

    public VehicleController(ListAvailableVehiclesUseCase listAvailableVehiclesUseCase) {
        this.listAvailableVehiclesUseCase = listAvailableVehiclesUseCase;
    }

    /**
     * Veículos à venda, ordenados por preço do mais barato para o mais caro.
     */
    @GetMapping("/available")
    public PageResponse<VehicleResponse> listAvailable(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return PageResponse.from(listAvailableVehiclesUseCase.execute(page, size), VehicleResponse::from);
    }
}
