package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.dto.ReviewCreateDTO;
import com.andrei.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<?> getAllReviews() {
        return ResponseEntity.ok(reviewService.getReviews());
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<?> getReviewById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(reviewService.getReviewById(uuid));
    }

    @GetMapping("/game/{gameId}")
    public ResponseEntity<?> getReviewsForGame(@PathVariable UUID gameId) {
        return ResponseEntity.ok(reviewService.getReviewsForGame(gameId));
    }

    @PreAuthorize("hasRole('ADMIN') or principal == #reviewDTO.authorId.toString()")
    @PostMapping
    public ResponseEntity<?> addReview(@Valid @RequestBody ReviewCreateDTO reviewDTO) throws ValidationException {
        return ResponseEntity.ok(reviewService.addReview(reviewDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or @reviewService.getReviewById(#uuid).author.id.toString() == principal")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateReview(@PathVariable UUID uuid, @Valid @RequestBody ReviewCreateDTO reviewDTO) throws ValidationException {
        return ResponseEntity.ok(reviewService.updateReview(uuid, reviewDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or @reviewService.getReviewById(#uuid).author.id.toString() == principal")
    @PatchMapping("/{uuid}")
    public ResponseEntity<?> patchReview(@PathVariable UUID uuid, @RequestBody Map<String, Object> updates) throws ValidationException {
        return ResponseEntity.ok(reviewService.patchReview(uuid, updates));
    }

    @PreAuthorize("hasRole('ADMIN') or @reviewService.getReviewById(#uuid).author.id.toString() == principal")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID uuid) throws ValidationException {
        reviewService.deleteReview(uuid);
        return ResponseEntity.ok("Review deleted successfully");
    }
}