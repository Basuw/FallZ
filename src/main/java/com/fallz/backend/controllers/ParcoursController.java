package com.fallz.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fallz.backend.dto.AddCoordonateDTO;
import com.fallz.backend.entities.Coordonates;
import com.fallz.backend.entities.Parcours;
import com.fallz.backend.services.CoordonatesService;
import com.fallz.backend.services.ParcoursService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

@RestController
@RequestMapping(path = "/parcours")
public class ParcoursController {

	@Autowired
	private CoordonatesService coordonatesService;

	@Autowired
	private ParcoursService parcoursService;

	@GetMapping(path = "/{userId}")
	public List<Parcours> getParoursByUserId(@PathVariable UUID userId) {
		return parcoursService.getParcoursByPersonId(userId);
	}

	@PostMapping(path = "/{userId}")
	public Parcours createParcours(@PathVariable UUID userId, @RequestBody List<AddCoordonateDTO> coordonates) {
		return parcoursService.createParcours(userId, coordonates);
	}

	@PatchMapping(path = "/{parcoursId}")
	public ResponseEntity<List<Coordonates>> addCoordonates(@PathVariable UUID parcoursId, @RequestBody @Valid @NotEmpty List<AddCoordonateDTO> coordonates) {
		return ResponseEntity.ok(parcoursService.addCoordonates(parcoursId, coordonates));
	}

	@GetMapping(path = "/{parcoursId}/coordonates")
	public ResponseEntity<List<Coordonates>> getCoordonatesByParcoursId(@PathVariable UUID parcoursId) {
		return ResponseEntity.ok(coordonatesService.getCoordonatesByParcoursId(parcoursId));
	}
}
