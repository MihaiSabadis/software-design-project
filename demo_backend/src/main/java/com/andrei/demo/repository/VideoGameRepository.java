package com.andrei.demo.repository;

import com.andrei.demo.model.VideoGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface VideoGameRepository extends JpaRepository<VideoGame, UUID> {
    Optional<VideoGame> findByTitle(String title);

    boolean existsByTitle(String newTitle);
}
