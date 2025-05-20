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
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "person")
public class Person {
    @Id
    @Column(name = "id_person", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @OneToOne(mappedBy = "person", cascade = CascadeType.ALL)
    private Device device;
}
