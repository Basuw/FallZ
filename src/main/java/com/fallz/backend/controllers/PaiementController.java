package com.fallz.backend.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fallz.backend.services.PaiementService;

@RestController
@RequestMapping(path = "/paiement")
public class PaiementController {
	
	@Autowired
	private PaiementService paiementService;
	
	@GetMapping(path = "/{userId}")
	public ResponseEntity<Float> getBill(@PathVariable String userId) {
		return ResponseEntity.ok(paiementService.getPaiement(UUID.fromString(userId)));
	}
}
