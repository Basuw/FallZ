package com.fallz.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterDTO {
	
	private String lastname;
	
	private String firstname;
	
	@NotBlank
	private String password;
	
	@NotBlank
	@Email
	private String mail;
}