package com.fallz.backend.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fallz.backend.entities.Device;

public interface DeviceRepository extends JpaRepository<Device, UUID> {

}
