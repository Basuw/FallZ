package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class LinkedDevice {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_person", nullable = false)
    private Person person;

    @ManyToOne
    @JoinColumn(name = "id_device", nullable = false)
    private Device device;

    // Getters and setters...
}
