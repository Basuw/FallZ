package com.fallz.backend.entities;

import java.util.List;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Device {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idDevice;
    
    @OneToOne
    @JoinColumn(name = "id_user", referencedColumnName = "idUser", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "device")
    private List<Parcours> parcours;
}
