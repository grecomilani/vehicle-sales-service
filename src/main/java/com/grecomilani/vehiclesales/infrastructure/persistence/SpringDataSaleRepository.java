package com.grecomilani.vehiclesales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface SpringDataSaleRepository extends JpaRepository<SaleEntity, UUID> {
}
