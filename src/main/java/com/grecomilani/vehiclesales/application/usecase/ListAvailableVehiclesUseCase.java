package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.PageResult;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;

/**
 * Lista os veículos à venda, do mais barato para o mais caro.
 */
public class ListAvailableVehiclesUseCase {

    private final VehicleRepository vehicleRepository;

    public ListAvailableVehiclesUseCase(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public PageResult<Vehicle> execute(int page, int size) {
        return vehicleRepository.findByStatusOrderByPriceAsc(VehicleStatus.AVAILABLE, page, size);
    }
}
