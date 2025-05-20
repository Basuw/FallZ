package com.fallz.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fallz.backend.entities.Device;
import com.fallz.backend.entities.Parcours;

@Repository
public interface ParcoursRepository extends JpaRepository<Parcours, UUID> {
	
	List<Parcours> findByDevice(Device device);
}
