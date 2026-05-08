package com.andrei.demo.repository;

import com.andrei.demo.model.Studio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudioRepository extends JpaRepository<Studio, UUID> {
    Optional<Studio> findByName(String name);

    Optional<Studio> findByRegistrationCode(String studioCode);
}