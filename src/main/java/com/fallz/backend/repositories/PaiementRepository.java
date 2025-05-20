package com.fallz.backend.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fallz.backend.entities.Paiement;
import com.fallz.backend.entities.User;

@Repository
public interface PaiementRepository extends JpaRepository<Paiement, UUID>{

	public Optional<Paiement> findByUser(User user);
}
