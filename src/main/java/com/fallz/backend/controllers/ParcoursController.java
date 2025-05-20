package com.fallz.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fallz.backend.entities.Parcours;
import com.fallz.backend.services.ParcoursService;

@RestController
@RequestMapping(path = "/parcours")
public class ParcoursController {
	
	@Autowired
	private ParcoursService parcoursService;

	@GetMapping(path = "/{id}")
	public List<Parcours> getParoursByUserId(@PathVariable UUID id) {
		return parcoursService.getParcoursByUserId(id);
	}
	@PostMapping(path = "/{userId}")
	public Parcours createMockParcours(@PathVariable UUID userId) {
		return parcoursService.createParcours(userId);
	}
}
