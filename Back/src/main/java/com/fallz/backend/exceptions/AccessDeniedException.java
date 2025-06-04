package com.fallz.backend.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class AccessDeniedException extends RuntimeException {

	private static final long serialVersionUID = -7967964776445731928L;

	public AccessDeniedException(String message) {
		super(message);
	}
}
