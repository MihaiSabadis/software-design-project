package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.ReviewCreateDTO;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.VideoGameRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static com.andrei.demo.model.Role.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ReviewControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private VideoGameRepository videoGameRepository;

    private Person testUser;
    private VideoGame testGame;

    @BeforeEach
    void setUp() {
        // Curățăm baza de date înainte de fiecare test pentru a nu avea conflicte
        personRepository.deleteAll();
        videoGameRepository.deleteAll();

        // 1. Creăm și salvăm un user real în baza de date H2
        testUser = new Person();
        testUser.setName("Integration Tester");
        testUser.setEmail("tester@example.com");
        testUser.setPassword("StrongPass123!@");
        testUser.setAge(25);
        testUser.setRole(PLAYER);
        testUser.setOwnedGames(new ArrayList<>());
        testUser = personRepository.save(testUser);

        // 2. Creăm și salvăm un joc real în baza de date H2
        testGame = new VideoGame();
        testGame.setTitle("Test Game");
        testGame.setPrice(59.99);
        testGame = videoGameRepository.save(testGame);
    }

    @Test
    void givenGameNotInLibrary_whenPostReview_thenReturnBadRequest() throws Exception {
        // ARRANGE: Creăm payload-ul de review (User-ul NU are jocul în librărie)
        ReviewCreateDTO payload = new ReviewCreateDTO();
        payload.setAuthorId(testUser.getId());
        payload.setGameId(testGame.getId());
        payload.setScore(1);
        payload.setComment("Terrible game!");

        // ACT & ASSERT: Simulam request-ul HTTP POST și ne așteptăm să pice (400 Bad Request)
        // Notă: Dacă GlobalExceptionHandler-ul tău returnează alt status pentru ValidationException, schimbă isBadRequest()
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void givenGameInLibrary_whenPostReview_thenReturnOk() throws Exception {
        // ARRANGE 1: Adăugăm jocul în librăria user-ului și salvăm în DB
        testUser.getOwnedGames().add(testGame);
        personRepository.save(testUser);

        // ARRANGE 2: Creăm payload-ul de review
        ReviewCreateDTO payload = new ReviewCreateDTO();
        payload.setAuthorId(testUser.getId());
        payload.setGameId(testGame.getId());
        payload.setScore(5);
        payload.setComment("Amazing game!");

        // ACT & ASSERT: Simulam request-ul HTTP POST și ne așteptăm să meargă (200 OK sau 201 Created)
        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk()); // Dacă metoda ta din Controller returnează 201, folosește isCreated()
    }
}