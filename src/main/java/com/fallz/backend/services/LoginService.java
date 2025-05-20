package com.fallz.backend.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fallz.backend.dto.ConnectDTO;
import com.fallz.backend.dto.RegisterDTO;
import com.fallz.backend.entities.Device;
import com.fallz.backend.entities.Person;
import com.fallz.backend.entities.User;
import com.fallz.backend.exceptions.AccessDeniedException;
import com.fallz.backend.exceptions.AlreadyExistsException;
import com.fallz.backend.repositories.DeviceRepository;
import com.fallz.backend.repositories.PersonRepository;
import com.fallz.backend.repositories.UserRepository;

@Service
public class LoginService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PersonRepository personRepository;

	@Autowired
	private DeviceRepository deviceRepository;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional(readOnly = true)
	public User connectUser(ConnectDTO dto) {
		final String mail = dto.getMail();

		Optional<User> user = userRepository.findByMail(mail);

		return user.map(u -> {
			try {
				Authentication authenticationRequest = new UsernamePasswordAuthenticationToken(mail, dto.getPassword());

				authenticationManager.authenticate(authenticationRequest);

				return u;
			} catch (BadCredentialsException e) {
				throw new AccessDeniedException("The couple user-password is incorrect");
			}
		}).orElseThrow(() -> new AccessDeniedException("The couple user-password is incorrect"));
	}

	@Transactional
	public User registerUser(RegisterDTO dto) {
		if (userRepository.existsByMail(dto.getMail())) {
			throw new AlreadyExistsException("A user with this email already exists.");
		}

		User user = userRepository.save(User.builder().mail(dto.getMail())
				.password(passwordEncoder.encode(dto.getPassword())).build());

		deviceRepository.save(Device.builder().build());

		return user;
	}
}
