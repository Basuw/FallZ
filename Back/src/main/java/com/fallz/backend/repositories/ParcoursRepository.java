package com.fallz.backend.repositories;

import com.fallz.backend.entities.Device;
import com.fallz.backend.entities.Parcours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParcoursRepository extends JpaRepository<Parcours, UUID> {
    // Find active parcours (where endDate is null)
    List<Parcours> findByEndDateIsNull();

    // Find active parcours for a specific device
    Optional<Parcours> findByDeviceIdAndEndDateIsNull(UUID deviceId);

	List<Parcours> findByDevice(Device device);
}
