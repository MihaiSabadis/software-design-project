package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Review;
import com.andrei.demo.model.ReviewCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.service.ReviewService;
import com.andrei.demo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final PersonService personService;
    private final JwtUtil jwtUtil;

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


    @PostMapping
    public ResponseEntity<?> addReview(@Valid @RequestBody ReviewCreateDTO reviewDTO,
                                       @RequestHeader("Authorization") String authHeader) throws ValidationException {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        // Check if a player is trying to spoof someone else's ID in the DTO
        if (!"ADMIN".equals(role) && !tokenId.equals(reviewDTO.getAuthorId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You cannot post a review as another user.");
        }

        return ResponseEntity.ok(reviewService.addReview(reviewDTO));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateReview(@PathVariable UUID uuid,
                                          @Valid @RequestBody ReviewCreateDTO reviewDTO,
                                          @RequestHeader("Authorization") String authHeader) throws ValidationException {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        Review existingReview = reviewService.getReviewById(uuid);

        if (!"ADMIN".equals(role) && !tokenId.equals(existingReview.getAuthor().getId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only edit your own reviews.");
        }

        return ResponseEntity.ok(reviewService.updateReview(uuid, reviewDTO));
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<?> patchReview(@PathVariable UUID uuid,
                                         @RequestBody Map<String, Object> updates,
                                         @RequestHeader("Authorization") String authHeader) throws ValidationException {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        Review existingReview = reviewService.getReviewById(uuid);

        if (!"ADMIN".equals(role) && !tokenId.equals(existingReview.getAuthor().getId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only edit your own reviews.");
        }

        return ResponseEntity.ok(reviewService.patchReview(uuid, updates));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteReview(@PathVariable UUID uuid,
                                          @RequestHeader("Authorization") String authHeader) throws ValidationException {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        Review existingReview = reviewService.getReviewById(uuid);

        if (!"ADMIN".equals(role) && !tokenId.equals(existingReview.getAuthor().getId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only delete your own reviews.");
        }

        reviewService.deleteReview(uuid);
        return ResponseEntity.ok("Review deleted successfully");
    }
}