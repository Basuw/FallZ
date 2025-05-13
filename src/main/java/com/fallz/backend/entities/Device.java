package com.fallz.backend.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class Device {
    @Id
    private UUID idDevice;
}
