package com.fallz.backend.services;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.fallz.backend.repositories.UserRepository;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		com.fallz.backend.entities.User user = userRepository.findByMail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
            
            return new User(
                user.getMail(),
                user.getPassword(),
                new ArrayList<>()
            );
	}
}
