package com.fallz.backend.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.exceptions.EntityNotFoundException;
import com.fallz.backend.repositories.PaiementRepository;
import com.fallz.backend.repositories.UserRepository;

@Service
public class PaiementService {

	@Autowired
	private PaiementRepository paiementRepository;

	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly = true)
	public Float getPaiement(UUID userId) {
		return paiementRepository
				.findByUserAndIsPaid(userRepository.findById(userId)
						.orElseThrow(() -> new EntityNotFoundException("User not found")), false)
				.orElseThrow(() -> new EntityNotFoundException("Paiement not found")).getAmount();
	}
}
