package com.fallz.backend.repositories;

import com.fallz.backend.entities.Parcours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ParcoursRepository extends JpaRepository<Parcours, UUID> {
    // Vous pouvez ajouter des méthodes spécifiques comme:
    // Optional<Parcours> findByActiveTrue();
}
