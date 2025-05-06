package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Paiement {
    @Id
    private UUID idPaiement;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    private BigDecimal amount;

    private LocalDateTime date;

    // Getters and setters...
}
