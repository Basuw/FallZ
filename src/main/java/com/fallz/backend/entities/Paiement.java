package com.fallz.backend.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Paiement {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idPaiement;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    private Float amount;

    private LocalDateTime date;

    // Getters and setters...
}
