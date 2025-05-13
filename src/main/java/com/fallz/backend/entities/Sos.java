package com.fallz.backend.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Setter
public class Sos {
    @Id
    private UUID idSos;

    @OneToOne
    @JoinColumn(name = "id_coordonates", nullable = false)
    private Coordonates coordonates;

    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "id_device", nullable = false)
    private LinkedDevice linkedDevice;

    // Default constructor
    public Sos() {
        this.date = LocalDateTime.now();
    }

    // Constructor with coordonates and active status
    public Sos(Coordonates coordonates) {
        this.coordonates = coordonates;
        this.date = LocalDateTime.now();
    }

    @PrePersist
    public void generateId() {
        if (this.idSos == null) {
            this.idSos = UUID.randomUUID();
        }
    }
}
