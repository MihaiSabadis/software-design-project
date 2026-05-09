package com.andrei.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "studio")
public class Studio {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Person> staff = new ArrayList<>();

    @OneToMany(mappedBy = "studio", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<VideoGame> games = new ArrayList<>();

    @Column(name = "registration_code", unique = true)
    private String registrationCode;

    @PrePersist
    protected void onCreate() {
        if (this.registrationCode == null) {
            // Generates a random 8-character alphanumeric code
            this.registrationCode = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}