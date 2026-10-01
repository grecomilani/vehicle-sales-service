package com.grecomilani.vehiclesales.infrastructure.web.vehicle;

import com.grecomilani.vehiclesales.application.usecase.ListAvailableVehiclesUseCase;
import com.grecomilani.vehiclesales.application.usecase.ListSoldVehiclesUseCase;
import com.grecomilani.vehiclesales.infrastructure.web.PageResponse;
import com.grecomilani.vehiclesales.infrastructure.web.VehicleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints públicos de consulta de veículos, consumidos pelo frontend.
 */
@Tag(name = "Veículos", description = "Consulta de veículos à venda e vendidos")
@RestController
@RequestMapping("/vehicles")
public class VehicleController {

    static final int MAX_PAGE_SIZE = 100;

    private final ListAvailableVehiclesUseCase listAvailableVehiclesUseCase;
    private final ListSoldVehiclesUseCase listSoldVehiclesUseCase;

    public VehicleController(ListAvailableVehiclesUseCase listAvailableVehiclesUseCase,
                             ListSoldVehiclesUseCase listSoldVehiclesUseCase) {
        this.listAvailableVehiclesUseCase = listAvailableVehiclesUseCase;
        this.listSoldVehiclesUseCase = listSoldVehiclesUseCase;
    }

    /**
     * Veículos à venda, ordenados por preço do mais barato para o mais caro.
     */
    @Operation(summary = "Lista os veículos à venda, do mais barato para o mais caro")
    @GetMapping("/available")
    public PageResponse<VehicleResponse> listAvailable(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return PageResponse.from(listAvailableVehiclesUseCase.execute(page, size), VehicleResponse::from);
    }

    /**
     * Veículos vendidos (pagamento confirmado), ordenados por preço do mais
     * barato para o mais caro.
     */
    @Operation(summary = "Lista os veículos vendidos, do mais barato para o mais caro")
    @GetMapping("/sold")
    public PageResponse<VehicleResponse> listSold(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(MAX_PAGE_SIZE) int size) {
        return PageResponse.from(listSoldVehiclesUseCase.execute(page, size), VehicleResponse::from);
    }
}
