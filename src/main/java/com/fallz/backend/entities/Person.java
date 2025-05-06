package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Person {
    @Id
    private UUID idPerson;

    @Column(unique = true, length = 50)
    private String firstname;

    @Column(unique = true, length = 50)
    private String lastname;

    // Getters and setters...
}
