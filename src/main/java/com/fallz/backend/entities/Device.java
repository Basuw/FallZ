package com.fallz.backend.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;
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
    @Column(name = "id_device", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "id_person", nullable = false)
    private Person person;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL)
    private List<Parcours> parcours = new ArrayList<>();
}
