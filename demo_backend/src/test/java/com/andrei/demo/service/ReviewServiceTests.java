package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Review;
import com.andrei.demo.model.dto.ReviewCreateDTO;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.ReviewRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PersonService personService;

    @Mock
    private VideoGameService videoGameService;

    @InjectMocks
    private ReviewService reviewService;

    private AutoCloseable closeable;

    private Person mockAuthor;
    private VideoGame mockGame;
    private ReviewCreateDTO reviewDTO;
    private UUID reviewId;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);

        reviewId = UUID.randomUUID();

        mockAuthor = new Person();
        mockAuthor.setId(UUID.randomUUID());
        mockAuthor.setOwnedGames(new ArrayList<>());

        mockGame = new VideoGame();
        mockGame.setId(UUID.randomUUID());

        reviewDTO = new ReviewCreateDTO();
        reviewDTO.setAuthorId(mockAuthor.getId());
        reviewDTO.setGameId(mockGame.getId());
        reviewDTO.setScore(5);
        reviewDTO.setComment("Great game!");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetReviews() {
        when(reviewRepository.findAll()).thenReturn(List.of(new Review()));
        List<Review> result = reviewService.getReviews();
        assertEquals(1, result.size());
    }

    @Test
    void testGetReviewById_Success() {
        Review review = new Review();
        review.setId(reviewId);
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        Review result = reviewService.getReviewById(reviewId);
        assertEquals(reviewId, result.getId());
    }

    @Test
    void testGetReviewById_NotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> reviewService.getReviewById(reviewId));
    }

    @Test
    void testAddReview_Success() throws ValidationException {
        mockAuthor.getOwnedGames().add(mockGame);

        when(personService.getPersonById(reviewDTO.getAuthorId())).thenReturn(mockAuthor);
        when(videoGameService.getVideoGameById(reviewDTO.getGameId())).thenReturn(mockGame);
        when(reviewRepository.existsByAuthorIdAndGameId(mockAuthor.getId(), mockGame.getId())).thenReturn(false);

        Review savedReview = new Review();
        savedReview.setScore(5);
        when(reviewRepository.save(any(Review.class))).thenReturn(savedReview);

        Review result = reviewService.addReview(reviewDTO);

        assertNotNull(result);
        assertEquals(5, result.getScore());
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    void testAddReview_NotOwned() {

        when(personService.getPersonById(reviewDTO.getAuthorId())).thenReturn(mockAuthor);
        when(videoGameService.getVideoGameById(reviewDTO.getGameId())).thenReturn(mockGame);


        ValidationException exception = assertThrows(ValidationException.class,
                () -> reviewService.addReview(reviewDTO));


        assertTrue(exception.getMessage().toLowerCase().contains("only review games that are in your library"));

        verify(reviewRepository, never()).save(any());

        verify(reviewRepository, never()).existsByAuthorIdAndGameId(any(), any());
    }

    @Test
    void testAddReview_AlreadyReviewed() {

        mockAuthor.getOwnedGames().add(mockGame);

        when(personService.getPersonById(reviewDTO.getAuthorId())).thenReturn(mockAuthor);
        when(videoGameService.getVideoGameById(reviewDTO.getGameId())).thenReturn(mockGame);

        when(reviewRepository.existsByAuthorIdAndGameId(mockAuthor.getId(), mockGame.getId())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> reviewService.addReview(reviewDTO));

        assertTrue(exception.getMessage().contains("already reviewed"));
        verify(reviewRepository, never()).save(any());
    }


    @Test
    void testUpdateReview_Success() throws ValidationException {
        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setScore(3);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(any(Review.class))).thenReturn(existingReview);

        Review result = reviewService.updateReview(reviewId, reviewDTO);

        assertEquals(5, result.getScore()); // Updated from 3 to 5
        verify(reviewRepository).save(existingReview);
    }

    @Test
    void testUpdateReview_NotFound() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> reviewService.updateReview(reviewId, reviewDTO));
    }

    @Test
    void testDeleteReview_Success() throws ValidationException {
        when(reviewRepository.existsById(reviewId)).thenReturn(true);
        doNothing().when(reviewRepository).deleteById(reviewId);

        reviewService.deleteReview(reviewId);

        verify(reviewRepository).deleteById(reviewId);
    }

    @Test
    void testDeleteReview_NotFound() {
        when(reviewRepository.existsById(reviewId)).thenReturn(false);

        assertThrows(ValidationException.class, () -> reviewService.deleteReview(reviewId));
        verify(reviewRepository, never()).deleteById(any());
    }

    @Test
    void testPatchReview_Success() throws ValidationException {
        Review existingReview = new Review();
        existingReview.setId(reviewId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("score", 4);
        updates.put("comment", "Updated comment");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(existingReview)).thenReturn(existingReview);

        Review result = reviewService.patchReview(reviewId, updates);

        assertEquals(4, result.getScore());
        assertEquals("Updated comment", result.getComment());
    }
}