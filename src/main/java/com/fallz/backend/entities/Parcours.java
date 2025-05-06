package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Parcours {
    @Id
    private UUID idParcours;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "id_device", nullable = false)
    private Device device;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    // Getters and setters...
}
