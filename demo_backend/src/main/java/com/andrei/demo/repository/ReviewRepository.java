package com.andrei.demo.repository;

import com.andrei.demo.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    // Custom method for our edge case validation!
    boolean existsByAuthorIdAndGameId(UUID authorId, UUID gameId);
}