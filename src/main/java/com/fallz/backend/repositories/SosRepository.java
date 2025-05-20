package com.fallz.backend.repositories;

import com.fallz.backend.entities.Sos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SosRepository extends JpaRepository<Sos, UUID> {
}
