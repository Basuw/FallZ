package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User {
    @Id
    private UUID idUser;

    private String password;

    @Column(unique = true)
    private String mail;

    @ManyToOne
    @JoinColumn(name = "id_person", nullable = false)
    private Person person;

    // Getters and setters...
}

