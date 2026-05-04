package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Review;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.ReviewRepository;
import com.andrei.demo.repository.VideoGameRepository;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class ReviewControllerIntegrationTests {

    private static final String FIXTURE_PATH = "src/test/resources/fixtures/";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private VideoGameRepository videoGameRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private Person testUser;
    private VideoGame testGame;
    private Review testReview;
    private String ownerToken;

    @BeforeEach
    void setUp() throws Exception {
        seedDatabase();
        ownerToken = jwtUtil.createToken(testUser);
    }

    private void seedDatabase() throws Exception {
        String gameData = loadFixture("game_seed.json");
        List<VideoGame> games = objectMapper.readValue(gameData, new TypeReference<>() {});
        videoGameRepository.saveAll(games);
        testGame = videoGameRepository.findAll().getFirst();

        String personData = loadFixture("person_seed.json");
        List<Person> people = objectMapper.readValue(personData, new TypeReference<>() {});
        people.getFirst().getOwnedGames().add(testGame);
        personRepository.saveAll(people);
        testUser = personRepository.findAll().getFirst();

        String reviewData = loadFixture("review_seed.json");
        List<Review> reviews = objectMapper.readValue(reviewData, new TypeReference<>() {});

        Review review = reviews.getFirst();
        review.setAuthor(testUser);
        review.setGame(testGame);
        testReview = reviewRepository.save(review);
    }

    @Test
    void testGetAllReviews() throws Exception {
        mockMvc.perform(get("/reviews")
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].score").value(5))
                .andExpect(jsonPath("$[0].comment").value("Amazing game!"));
    }

    @Test
    void testGetReviewById_Success() throws Exception {
        mockMvc.perform(get("/reviews/" + testReview.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5))
                .andExpect(jsonPath("$.comment").value("Amazing game!"));
    }

    @Test
    void testAddReview_Success() throws Exception {
        VideoGame newGame = new VideoGame();
        newGame.setTitle("Cyberpunk 2077");
        newGame.setPrice(59.99);
        newGame.setDeveloper("CD Projekt Red");
        newGame = videoGameRepository.save(newGame);

        testUser.getOwnedGames().add(newGame);
        personRepository.save(testUser);

        long initialCount = reviewRepository.count();

        String template = loadFixture("new_review_template.json");
        String newReviewJson = String.format(template, testUser.getId().toString(), newGame.getId().toString());

        mockMvc.perform(post("/reviews")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newReviewJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(4))
                .andExpect(jsonPath("$.comment").value("Pretty good, lots of neon."));

        assertEquals(initialCount + 1, reviewRepository.count());
    }

    @Test
    void testUpdateReview_Success() throws Exception {
        String template = loadFixture("update_review_template.json");
        String updateReviewJson = String.format(template, testUser.getId().toString(), testGame.getId().toString());

        mockMvc.perform(put("/reviews/" + testReview.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateReviewJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(2))
                .andExpect(jsonPath("$.comment").value("I changed my mind, too many bugs."));

        Review updatedInDb = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertEquals(2, updatedInDb.getScore());
    }

    @Test
    void testPatchReview_Success() throws Exception {
        String patchJson = loadFixture("patch_review.json");

        mockMvc.perform(patch("/reviews/" + testReview.getId())
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(3))
                .andExpect(jsonPath("$.comment").value("Amazing game!"));

        Review patchedInDb = reviewRepository.findById(testReview.getId()).orElseThrow();
        assertEquals(3, patchedInDb.getScore());
    }

    @Test
    void testDeleteReview_Success() throws Exception {
        long initialCount = reviewRepository.count();

        mockMvc.perform(delete("/reviews/" + testReview.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        assertEquals(initialCount - 1, reviewRepository.count());
        assertFalse(reviewRepository.existsById(testReview.getId()));
    }

    private String loadFixture(String fileName) throws IOException {
        return Files.readString(Paths.get(FIXTURE_PATH + fileName));
    }
}