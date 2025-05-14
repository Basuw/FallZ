package com.fallz.backend.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Setter
@Getter
public class Coordonates {
    @Id
    private UUID idCoordonates;

    private double latitude;
    private double longitude;
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "id_parcours", nullable = false)
    private Parcours parcours;

    // Default constructor
    public Coordonates() {}

    // Constructor with latitude and longitude
    public Coordonates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PrePersist
    public void generateId() {
        if (this.idCoordonates == null) {
            this.idCoordonates = UUID.randomUUID();
        }
    }
}
