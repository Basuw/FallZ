package com.fallz.backend.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fallz.backend.entities.Coordonates;
import com.fallz.backend.entities.Parcours;

@Repository
public interface CoordonatesRepository extends JpaRepository<Coordonates, UUID> {
	
	List<Coordonates> findByParcours(Parcours parcours);
}
