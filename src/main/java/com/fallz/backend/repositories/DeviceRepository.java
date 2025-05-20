package com.fallz.backend.repositories;

import java.util.UUID;

import com.fallz.backend.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fallz.backend.entities.Device;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {

    Device findByPerson(Person person);
}
