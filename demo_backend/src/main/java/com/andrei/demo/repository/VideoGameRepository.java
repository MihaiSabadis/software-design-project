package com.andrei.demo.repository;

import com.andrei.demo.model.VideoGame;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoGameRepository extends JpaRepository<VideoGame, UUID> {
    Optional<VideoGame> findByTitle(String title);

    boolean existsByTitle(String newTitle);
    boolean existsById(@NonNull UUID id);

    @Query("SELECT v FROM VideoGame v WHERE " +
            "(:searchTitle IS NULL OR LOWER(v.title) LIKE LOWER(:searchTitle)) AND " +
            "(:studioName IS NULL OR v.studio.name = :studioName) AND " +
            "(:maxPrice IS NULL OR v.price <= :maxPrice)")
    List<VideoGame> searchAndFilterGames(
            @Param("searchTitle") String searchTitle,
            @Param("studioName") String studioName,
            @Param("maxPrice") Double maxPrice,
            Sort sort
    );
}