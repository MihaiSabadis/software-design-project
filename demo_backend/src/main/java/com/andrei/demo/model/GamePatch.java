package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class GamePatch {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "video_game_id")
    private VideoGame videoGame;

    @Column(nullable = false)
    private String version;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private LocalDate releaseDate;
}