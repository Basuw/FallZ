package com.fallz.backend.entities;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Coordonates {

    @Id
    @Column(name = "id_coordonates", nullable = false)
    private UUID idCoordonates;

    @ManyToOne
    @JoinColumn(name = "id_parcours", nullable = false)
    @JsonIgnore
    private Parcours parcours;

    @Column(name = "latitude")
    private double latitude;

    @Column(name = "longitude")
    private double longitude;

    @Column(name = "date")
    private LocalDateTime date;

    @OneToOne(mappedBy = "coordonates", cascade = CascadeType.ALL)
    private Fall fall;

    // Constructor with latitude and longitude
    public Coordonates(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
