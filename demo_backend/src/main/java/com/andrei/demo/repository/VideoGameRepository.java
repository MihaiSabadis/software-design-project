package com.andrei.demo.repository;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.VideoGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

public interface VideoGameRepository extends JpaRepository<VideoGame, UUID> {
    //to find duplicate games
    boolean existsByTitle(String title);

    Optional<VideoGame> findByTitle(String title);
}
