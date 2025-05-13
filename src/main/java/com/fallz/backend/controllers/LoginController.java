package com.fallz.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fallz.backend.dto.ConnectDTO;
import com.fallz.backend.dto.RegisterDTO;
import com.fallz.backend.entities.User;
import com.fallz.backend.services.LoginService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;

@RestController
@RequestMapping(path = "/login")
public class LoginController {
	
	@Autowired
	private LoginService loginService;

	@PostMapping(path = "/connect")
	public ResponseEntity<User> connectUser(@RequestBody @Valid ConnectDTO dto) throws EntityNotFoundException {
		return ResponseEntity.ok(loginService.connectUser(dto));
	}

	@PostMapping(path = "/register")
	public ResponseEntity<User> registerUser(@RequestBody @Valid RegisterDTO dto) {
		return ResponseEntity.ok(loginService.registerUser(dto));
	}
}
