package com.fallz.backend.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.entities.Coordonates;
import com.fallz.backend.exceptions.EntityNotFoundException;
import com.fallz.backend.repositories.CoordonatesRepository;
import com.fallz.backend.repositories.ParcoursRepository;

@Service
public class CoordonatesService {

	@Autowired
	private CoordonatesRepository coordonatesRepository;

	@Autowired
	private ParcoursRepository parcoursRepository;

	@Transactional(readOnly = true)
	public List<Coordonates> getCoordonatesByParcoursId(UUID parcoursId) {
		return coordonatesRepository.findByParcours(parcoursRepository.findById(parcoursId)
				.orElseThrow(() -> new EntityNotFoundException("Parcours not found")));
	}
}
