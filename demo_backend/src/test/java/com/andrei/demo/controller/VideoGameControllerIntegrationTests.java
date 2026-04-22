package com.andrei.demo.controller;

import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.VideoGameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

// Matching your exact Jackson imports from the Person tests
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class VideoGameControllerIntegrationTests {

    private static final String FIXTURE_PATH = "src/test/resources/fixtures/";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VideoGameRepository videoGameRepository;

    private VideoGame testGame;

    @BeforeEach
    void setUp() throws Exception {
        seedDatabase();

        testGame = videoGameRepository.findAll().getFirst();
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("game_seed.json");
        List<VideoGame> games = objectMapper.readValue(seedDataJson, new TypeReference<>() {});
        videoGameRepository.saveAll(games);
    }

    @Test
    void testGetAllVideoGames() throws Exception {
        mockMvc.perform(get("/videogames"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Red Dead Redemption 2"));
    }

    @Test
    void testGetVideoGameById_Success() throws Exception {
        mockMvc.perform(get("/videogames/" + testGame.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Red Dead Redemption 2"))
                .andExpect(jsonPath("$.price").value(59.99));
    }

    @Test
    void testAddVideoGame_Success() throws Exception {
        long initialCount = videoGameRepository.count();

        String newGameJson = loadFixture("new_videogame.json");

        mockMvc.perform(post("/videogames")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newGameJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Stardew Valley"))
                .andExpect(jsonPath("$.price").value(14.99))
                .andExpect(jsonPath("$.developer").value("ConcernedApe"));

        assertEquals(initialCount + 1, videoGameRepository.count());
        assertTrue(videoGameRepository.existsByTitle("Stardew Valley"));
    }

    @Test
    void testUpdateVideoGame_Success() throws Exception {
        String updateJson = loadFixture("update_videogame.json");

        mockMvc.perform(put("/videogames/" + testGame.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(79.99))
                .andExpect(jsonPath("$.title").value("Red Dead Redemption 2 - Special Edition"));

        VideoGame updatedInDb = videoGameRepository.findById(testGame.getId()).orElseThrow();
        assertEquals("Red Dead Redemption 2 - Special Edition", updatedInDb.getTitle());
        assertEquals(79.99, updatedInDb.getPrice());
    }

    @Test
    void testDeleteVideoGame_Success() throws Exception {
        long initialCount = videoGameRepository.count();

        mockMvc.perform(delete("/videogames/" + testGame.getId()))
                .andExpect(status().isOk());

        assertEquals(initialCount - 1, videoGameRepository.count());
        assertFalse(videoGameRepository.existsById(testGame.getId()));
    }

    private String loadFixture(String fileName) throws IOException {
        return Files.readString(Paths.get(FIXTURE_PATH + fileName));
    }
}