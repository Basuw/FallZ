package com.fallz.backend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Coordonates {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idCoordonates;

    private double latitude;
    private double longitude;
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "id_parcours", nullable = false)
    private Parcours parcours;

    // Constructor with latitude and longitude
    public Coordonates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
