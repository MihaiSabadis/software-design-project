package com.andrei.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Entity
@Data
@Table(name = "video_game")
public class VideoGame {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "title", nullable = false, unique = true)
    private String title="";

    @ManyToOne
    @JoinColumn(name = "studio_id", nullable = false)
    private Studio studio;

    @Column(name = "price", nullable = false)
    private Double price=0.0;

    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;

    @ManyToMany(mappedBy = "ownedGames")
    @JsonIgnore
    private List<Person> owners = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "videoGame", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GamePatch> patches = new ArrayList<>();
}
