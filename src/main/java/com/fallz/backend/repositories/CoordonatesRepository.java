package com.fallz.backend.repositories;

import com.fallz.backend.entities.Coordonates;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CoordonatesRepository extends JpaRepository<Coordonates, UUID> {
}
