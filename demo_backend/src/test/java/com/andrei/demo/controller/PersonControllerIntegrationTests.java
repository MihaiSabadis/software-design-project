package com.andrei.demo.controller;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Role;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.VideoGameRepository;
import com.andrei.demo.util.JwtUtil;
import jakarta.transaction.Transactional;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private VideoGameRepository videoGameRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String FIXTURE_PATH = "src/test/resources/fixtures/";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        personRepository.deleteAll();
        personRepository.flush();
        seedDatabase();

        Person adminUser = new Person();
        adminUser.setId(UUID.randomUUID());
        adminUser.setEmail("admin@test.com");
        adminUser.setRole(Role.valueOf("ADMIN"));
        adminToken = jwtUtil.createToken(adminUser);
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("person_seed.json");
        List<Person> people = objectMapper.readValue(seedDataJson, new TypeReference<>() {});
        personRepository.saveAll(people);
    }

    @Test
    void testGetPeople() throws Exception {
        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer " + adminToken)) // <-- Added space here!
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name", Matchers.containsInAnyOrder("John Doe", "Jane Doe")))
                .andExpect(jsonPath("$[*].age", Matchers.containsInAnyOrder(30, 25)))
                .andExpect(jsonPath("$[*].email", Matchers.containsInAnyOrder("john.doe@example.com", "jane.doe@example.com")));
    }

    @Test
    void testAddPerson_ValidPayload() throws Exception {
        String validPersonJson = loadFixture("valid_person.json");
        long initialCount = personRepository.count();

        // NO TOKEN NEEDED HERE! This is the public Registration endpoint.
        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.password", Matchers.startsWith("$2")))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"))
                .andExpect(jsonPath("$.role").value("PLAYER"));

        org.junit.jupiter.api.Assertions.assertEquals(initialCount + 1, personRepository.count());
    }

    @Test
    void testAddPerson_InvalidPayload() throws Exception {
        String invalidPersonJson = loadFixture("invalid_person.json");

        mockMvc.perform(post("/person")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPersonJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name should be between 2 and 100 characters"))
                .andExpect(jsonPath("$.password").value("Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character"))
                .andExpect(jsonPath("$.age").value("Age is required"))
                .andExpect(jsonPath("$.email").value("Email is required"));
    }

    @Test
    void testGetPersonById_Success() throws Exception {
        Person person = personRepository.findAll().getFirst();
        String ownerToken = jwtUtil.createToken(person);

        mockMvc.perform(get("/person/" + person.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(person.getName()))
                .andExpect(jsonPath("$.email").value(person.getEmail()));
    }

    @Test
    void testGetPersonByEmail_Success() throws Exception {
        String email = "john.doe@example.com";
        Person person = personRepository.findAll().getFirst(); // Assuming this is John
        String ownerToken = jwtUtil.createToken(person);

        mockMvc.perform(get("/person/email/" + email)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testUpdatePerson_Success() throws Exception {
        Person existingPerson = personRepository.findAll().getFirst();
        UUID id = existingPerson.getId();
        String ownerToken = jwtUtil.createToken(existingPerson);

        String updatedPersonJson = """
            {
                "name": "John Updated",
                "age": 35,
                "email": "john.updated@example.com",
                "password": "StrongPassword123!",
                "role": "PLAYER"
            }
            """;

        mockMvc.perform(put("/person/" + id)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPersonJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));

        Person updatedInDb = personRepository.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("John Updated", updatedInDb.getName());
    }

    @Test
    void testDeletePerson_Success() throws Exception {
        Person person = personRepository.findAll().getFirst();
        UUID id = person.getId();
        long initialCount = personRepository.count();
        String ownerToken = jwtUtil.createToken(person);

        mockMvc.perform(delete("/person/" + id)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(initialCount - 1, personRepository.count());
        org.junit.jupiter.api.Assertions.assertFalse(personRepository.existsById(id));
    }

    @Test
    void testAddGameToLibrary_Success() throws Exception {
        VideoGame game = new VideoGame();
        game.setTitle("Elden Ring");
        game.setPrice(59.99);
        game.setDeveloper("From Software Inc.");
        game = videoGameRepository.save(game);

        Person person = personRepository.findAll().getFirst();
        String ownerToken = jwtUtil.createToken(person);

        mockMvc.perform(post("/person/" + person.getId() + "/games/" + game.getId())
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownedGames.length()").value(1))
                .andExpect(jsonPath("$.ownedGames[0].title").value("Elden Ring"));

        Person updatedPerson = personRepository.findById(person.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertTrue(
                updatedPerson.getOwnedGames().stream().anyMatch(g -> g.getTitle().equals("Elden Ring"))
        );
    }

    private String loadFixture(String fileName) throws IOException {
        return Files.readString(Paths.get(FIXTURE_PATH + fileName));
    }
}