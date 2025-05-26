package com.fallz.backend.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.entities.Device;
import com.fallz.backend.entities.Person;
import com.fallz.backend.entities.User;
import com.fallz.backend.exceptions.AccessDeniedException;
import com.fallz.backend.exceptions.EntityNotFoundException;
import com.fallz.backend.repositories.DeviceRepository;
import com.fallz.backend.repositories.PersonRepository;
import com.fallz.backend.repositories.UserRepository;

@Service
public class DeviceService {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private DeviceRepository deviceRepository;
	
	@Autowired
	private PersonRepository personRepository;

	@Transactional(readOnly = true)
	public List<Device> getDevices(UUID userId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));

		List<Person> persons = user.getPersons();

		return persons.stream().map(person -> {
			return person.getDevice();
		}).collect(Collectors.toList());
	}
	
	@Transactional
	public Device addDevice(UUID userId, Device device) {
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
		Person person = device.getPerson();
		
		if(user.getPersons().contains(person)) {
			throw new AccessDeniedException("Person already exists");
		}
		
		Person savedPerson = personRepository.save(person);
		
		savedPerson.setUser(user);
		user.getPersons().add(savedPerson);
		device.setPerson(savedPerson);
		
		Device savedDevice = deviceRepository.save(device);
		userRepository.save(user);
		return savedDevice;
	}
	
	@Transactional
	public void deleteDevice(UUID userId, UUID deviceId) {
		User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
		
		Device device = deviceRepository.findById(deviceId).orElseThrow(() -> new EntityNotFoundException("Device not found"));
		
		if(!user.getPersons().contains(device.getPerson())) {
			throw new AccessDeniedException("Person does not belong to user");
		}
		
		deviceRepository.delete(device);
		user.getPersons().remove(device.getPerson());
		userRepository.save(user);
	}

}
