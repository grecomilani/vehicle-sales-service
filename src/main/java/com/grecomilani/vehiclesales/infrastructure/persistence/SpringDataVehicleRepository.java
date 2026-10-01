package com.grecomilani.vehiclesales.infrastructure.persistence;

import com.grecomilani.vehiclesales.domain.vehicle.VehicleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataVehicleRepository extends JpaRepository<VehicleEntity, UUID> {

    Page<VehicleEntity> findByStatus(VehicleStatus status, Pageable pageable);
}
