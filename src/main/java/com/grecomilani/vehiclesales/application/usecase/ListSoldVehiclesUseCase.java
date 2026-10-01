package com.grecomilani.vehiclesales.application.usecase;

import com.grecomilani.vehiclesales.application.port.PageResult;
import com.grecomilani.vehiclesales.application.port.VehicleRepository;
import com.grecomilani.vehiclesales.domain.vehicle.Vehicle;
import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;

/**
 * Lista os veículos vendidos (pagamento confirmado), do mais barato para o mais caro.
 */
public class ListSoldVehiclesUseCase {

    private final VehicleRepository vehicleRepository;

    public ListSoldVehiclesUseCase(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public PageResult<Vehicle> execute(int page, int size) {
        return vehicleRepository.findByStatusOrderByPriceAsc(VehicleStatus.SOLD, page, size);
    }
}
