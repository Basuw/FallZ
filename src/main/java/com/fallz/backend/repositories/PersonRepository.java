package com.fallz.backend.repositories;

import com.fallz.backend.entities.Person;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PersonRepository extends JpaRepository<Person, UUID> {
    // Custom queries can be added here
}
