package com.fallz.backend.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parcours {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idParcours;

    @ManyToOne
    @JoinColumn(name = "id_device", nullable = false)
    @JsonIgnore
    private Device device;

    private LocalDateTime startDate;

    private LocalDateTime endDate;
    
    @OneToMany(mappedBy = "parcours")
    @JsonIgnore
    private List<Coordonates> coordonates;
}
