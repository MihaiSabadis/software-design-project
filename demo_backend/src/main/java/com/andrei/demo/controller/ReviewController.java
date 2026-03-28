package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Review;
import com.andrei.demo.model.ReviewCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/reviews")
@AllArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final PersonService personService;

    @GetMapping
    public ResponseEntity<List<Review>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getReviews());
    }

    @GetMapping("/{uuid}")
    public Review getReviewById(@PathVariable UUID uuid) {
        return reviewService.getReviewById(uuid);
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody ReviewCreateDTO reviewDTO) throws ValidationException {
        return reviewService.addReview(reviewDTO);
    }

    @PutMapping("/{uuid}")
    public Review updateReview(@PathVariable UUID uuid, @Valid @RequestBody ReviewCreateDTO reviewDTO) throws ValidationException {
        return reviewService.updateReview(uuid, reviewDTO);
    }

    @PatchMapping("/{uuid}")
    public Review updateReview(@PathVariable UUID uuid, @RequestBody Map<String,
                Object> updates) throws ValidationException {
        return reviewService.patchReview(uuid, updates);

    }

    @DeleteMapping("/{uuid}")
    public void deleteReview(@PathVariable UUID uuid) throws ValidationException {
        reviewService.deleteReview(uuid);
    }
}