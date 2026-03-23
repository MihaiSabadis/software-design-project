package com.andrei.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity
@Data
@Table(name = "review")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "score", nullable = false)
    private Integer score;

    @Column(name = "comment", length = 700)
    private String comment;

    //many reviews belong to one person
    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    @JsonIgnore
    private Person author;

    //many reviews belong to one video game
    @ManyToOne
    @JoinColumn(name = "video_game_id", nullable = false)
    @JsonIgnore
    private VideoGame game;
}
