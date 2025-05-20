package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
public class Fall {

    @Id
    @Column(name = "id_fall", nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "id_coordonates", nullable = false)
    private Coordonates coordonates;

    @ManyToOne
    @JoinColumn(name = "id_person", nullable = false)
    private Person person;
}
