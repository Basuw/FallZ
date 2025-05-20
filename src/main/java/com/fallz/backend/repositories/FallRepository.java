package com.fallz.backend.repositories;

import com.fallz.backend.entities.Fall;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FallRepository extends JpaRepository<Fall, UUID> {
    // Custom queries can be added here
}
