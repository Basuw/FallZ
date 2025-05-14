package com.fallz.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConnectDTO {
	
	@NotBlank
	@Email
	private String mail;
	
	@NotBlank
	private String password;
}
