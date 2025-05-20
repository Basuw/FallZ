package com.fallz.backend.services;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import com.fallz.backend.entities.*;
import com.fallz.backend.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.dto.AddCoordonateDTO;
import com.fallz.backend.exceptions.EntityNotFoundException;

@Service
public class ParcoursService {

	@Autowired
	private ParcoursRepository parcoursRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private CoordonatesRepository coordonatesRepository;
    @Autowired
    private PersonRepository personRepository;
    @Autowired
    private DeviceRepository deviceRepository;

	@Transactional(readOnly = true)
	public List<Parcours> getParcoursByPersonId(UUID id) {
		Person person = personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found"));
		return parcoursRepository.findByDevice(person.getDevice());
	}

	@Transactional
	public Parcours createParcours(UUID id, List<AddCoordonateDTO> coordonates) {
		Person person = personRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Person not found"));

		Instant startDate = Instant.now();

		long minSeconds = Duration.ofMinutes(30).getSeconds();
		long maxSeconds = Duration.ofHours(5).getSeconds();

		long randomSeconds = ThreadLocalRandom.current().nextLong(minSeconds, maxSeconds + 1);

		Parcours parcours = Parcours.builder().device(person.getDevice())
				.startDate(LocalDateTime.ofInstant(startDate, ZoneId.systemDefault()))
				.endDate(LocalDateTime.ofInstant(startDate.plusSeconds(randomSeconds), ZoneId.systemDefault())).build();

		parcours = parcoursRepository.save(parcours);

		if (coordonates != null && !coordonates.isEmpty()) {
			addCoordonatesToParcours(parcours, coordonates);
		}

		return parcours;
	}

	@Transactional
	public List<Coordonates> addCoordonates(UUID parcoursId, List<AddCoordonateDTO> coordonates) {
		Parcours parcours = parcoursRepository.findById(parcoursId)
				.orElseThrow(() -> new EntityNotFoundException("Parcours not found"));

		addCoordonatesToParcours(parcours, coordonates);
		return parcoursRepository.save(parcours).getCoordonates();
	}

	private void addCoordonatesToParcours(Parcours parcours, List<AddCoordonateDTO> coordonates) {
		List<Coordonates> existingCoordonates = parcours.getCoordonates() == null ? new ArrayList<>()
				: parcours.getCoordonates();

		existingCoordonates.addAll(coordonates.stream().map(coordonate -> {
			Coordonates newCoordonates = new Coordonates(coordonate.getLatitude(), coordonate.getLongitude());
			newCoordonates.setParcours(parcours);
			return coordonatesRepository.save(newCoordonates);
		}).collect(Collectors.toList()));
	}
}
