package com.fallz.backend.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
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
    @Column(name = "id_parcours", nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "id_device", nullable = false)
    @JsonIgnore
    private Device device;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @OneToMany(mappedBy = "parcours", cascade = CascadeType.ALL)
    private List<Coordonates> coordonates = new ArrayList<>();
}
