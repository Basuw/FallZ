package com.fallz.backend.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.entities.Parcours;
import com.fallz.backend.entities.User;
import com.fallz.backend.exceptions.EntityNotFoundException;
import com.fallz.backend.repositories.ParcoursRepository;
import com.fallz.backend.repositories.UserRepository;

@Service
public class ParcoursService {
	
	@Autowired
	private ParcoursRepository parcoursRepository;
	
	@Autowired
	private UserRepository userRepository;

	@Transactional(readOnly = true)
	public List<Parcours> getParcoursByUserId(UUID id){
		User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
		return parcoursRepository.findByDevice(user.getDevice());
	}
	
	@Transactional
	public Parcours createParcours(UUID id){
		User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		Instant startDate = Instant.now();
		
        long minSeconds = Duration.ofMinutes(30).getSeconds();
        long maxSeconds = Duration.ofHours(5).getSeconds();
        
        long randomSeconds = ThreadLocalRandom.current().nextLong(minSeconds, maxSeconds + 1);
		
		Parcours parcours = Parcours.builder()
				.device(user.getDevice())
				.startDate(LocalDateTime.ofInstant(startDate, ZoneId.systemDefault()))
				.endDate(LocalDateTime.ofInstant(startDate.plusSeconds(randomSeconds), ZoneId.systemDefault()))
				.build();
		
		return parcoursRepository.save(parcours);
	}
}
