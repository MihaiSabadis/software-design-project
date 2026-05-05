package com.andrei.demo.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class PriceHistory {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "video_game_id")
    private VideoGame videoGame;

    private Double price;
    private LocalDate recordedAt; // Data la care a fost valabil acest preț
}