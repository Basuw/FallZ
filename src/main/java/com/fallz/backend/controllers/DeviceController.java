package com.fallz.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fallz.backend.entities.Device;
import com.fallz.backend.services.DeviceService;

@RestController
@RequestMapping("/device")
public class DeviceController {
	
	@Autowired
	private DeviceService deviceService;
	
	@GetMapping(path = "/{userId}")
	public ResponseEntity<List<Device>> getDevices(@PathVariable String userId) {
		return ResponseEntity.ok(deviceService.getDevices(UUID.fromString(userId)));
	}
	
	@PostMapping(path = "/{userId}")
	public ResponseEntity<Device> addDevice(@PathVariable String userId, @RequestBody Device device) {
		return ResponseEntity.ok(deviceService.addDevice(UUID.fromString(userId), device));
	}
	
	//supprimer une personne
	@DeleteMapping(path = "/{userId}/{deviceId}")
	public void deleteDevice(@PathVariable String userId, @PathVariable String deviceId) {
		deviceService.deleteDevice(UUID.fromString(userId), UUID.fromString(deviceId));
	}
}
