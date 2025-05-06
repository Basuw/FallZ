package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Fall {
    @Id
    private UUID idFall;

    @ManyToOne
    @JoinColumn(name = "id_parcours", nullable = false)
    private Parcours parcours;

    private LocalDateTime date;

    // Getters and setters...
}
