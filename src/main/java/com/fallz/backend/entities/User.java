package com.fallz.backend.entities;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "\"user\"")
public class User {
	
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID idUser;

    @JsonIgnore
    private String password;

    @Column(unique = true)
    private String mail;

    @ManyToOne
    @JoinColumn(name = "id_person", nullable = false)
    private Person person;
    
    @OneToOne(mappedBy = "user")
    private Device device;
}

