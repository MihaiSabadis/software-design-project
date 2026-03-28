package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Review;
import com.andrei.demo.model.ReviewCreateDTO;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final PersonService personService;
    private final VideoGameService videoGameService;

    public List<Review> getReviews() {
        return reviewRepository.findAll();
    }

    public Review getReviewById(UUID id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Review with ID " + id + " not found."));
    }

    public Review addReview(ReviewCreateDTO reviewDTO) throws ValidationException {

        Person author = personService.getPersonById(reviewDTO.getAuthorId());
        VideoGame game = videoGameService.getVideoGameById(reviewDTO.getGameId());

        // edge case: Did this person already review this game?
        if (reviewRepository.existsByAuthorIdAndGameId(author.getId(), game.getId())) {
            throw new ValidationException("User has already reviewed this game!");
        }

        Review review = new Review();
        review.setScore(reviewDTO.getScore());
        review.setComment(reviewDTO.getComment());
        review.setAuthor(author);
        review.setGame(game);

        return reviewRepository.save(review);
    }

    public Review updateReview(UUID uuid, ReviewCreateDTO reviewDTO) throws ValidationException {
        Optional<Review> reviewOptional = reviewRepository.findById(uuid);

        if (reviewOptional.isEmpty()){
            throw new ValidationException("Review with ID " + uuid + " not found.");
        }

        Review existingReview = reviewOptional.get();

        // only allow updating the score and comment, not the author or game
        existingReview.setScore(reviewDTO.getScore());
        existingReview.setComment(reviewDTO.getComment());

        return reviewRepository.save(existingReview);
    }

    public void deleteReview(UUID id) throws ValidationException {
        if (!reviewRepository.existsById(id)) {
            throw new ValidationException("Cannot delete. Review with ID " + id + " not found.");
        }
        reviewRepository.deleteById(id);
    }

    public Review patchReview(UUID uuid, Map<String, Object> updates) throws ValidationException {
        Review existingReview = reviewRepository.findById(uuid)
                .orElseThrow(()-> new ValidationException("Review with ID " + uuid + " not found."));

        if(updates.containsKey("score")){
            existingReview.setScore((Integer) updates.get("score"));
        }

        if(updates.containsKey("comment")){
            existingReview.setComment((String) updates.get("comment"));
        }

        return reviewRepository.save(existingReview);
    }
}