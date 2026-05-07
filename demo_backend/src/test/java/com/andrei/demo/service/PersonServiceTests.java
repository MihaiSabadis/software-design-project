package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.dto.PersonCreateDTO;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.PasswordUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

class PersonServiceTests {

    @Mock
    private PersonRepository personRepository;

    @Mock
    private com.andrei.demo.repository.VideoGameRepository videoGameRepository;

    @Mock
    private PasswordUtil passwordUtil;

    @InjectMocks
    private PersonService personService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetPeople() {
        // given:
        List<Person> people = List.of(new Person(), new Person());

        // when:
        when(personRepository.findAll()).thenReturn(people);
        List<Person> result = personService.getPeople();

        // then:
        assertEquals(2, result.size());
        verify(personRepository, times(1)).findAll();
        assertEquals(people, result);
    }

    @Test
    void testAddPerson() throws ValidationException {
        // given:
        PersonCreateDTO personDTO = new PersonCreateDTO();
        personDTO.setName("John");
        personDTO.setPassword("password");
        personDTO.setAge(30);
        personDTO.setEmail("john@example.com");

        when(passwordUtil.hashPassword("password")).thenReturn("hashed-password");
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Person result = personService.addPerson(personDTO);

        // then:
        assertEquals("John", result.getName());
        assertEquals(30, result.getAge());
        assertEquals("john@example.com", result.getEmail());
        assertEquals("hashed-password", result.getPassword());
        verify(passwordUtil, times(1)).hashPassword("password");
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testUpdatePerson() throws ValidationException {
        // given:
        UUID uuid = UUID.randomUUID();
        Person existingPerson = new Person();
        existingPerson.setId(uuid);
        existingPerson.setName("John");
        existingPerson.setAge(30);
        existingPerson.setEmail("john@example.com");
        existingPerson.setPassword("old-hash");

        Person updatePayload = new Person();
        updatePayload.setId(uuid);
        updatePayload.setName("Jane");
        updatePayload.setAge(25);
        updatePayload.setEmail("jane@example.com");
        updatePayload.setPassword("newpassword");

        when(personRepository.findById(uuid)).thenReturn(Optional.of(existingPerson));
        when(passwordUtil.hashPassword("newpassword")).thenReturn("new-hash");
        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Person result = personService.updatePerson(uuid, updatePayload);

        // then:
        assertEquals("Jane", result.getName());
        assertEquals(25, result.getAge());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("new-hash", result.getPassword());

        verify(personRepository, times(1)).findById(uuid);
        verify(passwordUtil, times(1)).hashPassword("newpassword");
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testUpdatePersonNotFound() {
        // given:
        UUID uuid = UUID.randomUUID();
        Person person = new Person();

        // when:
        when(personRepository.findById(uuid)).thenReturn(Optional.empty());

        // then:
        assertThrows(ValidationException.class, () -> personService.updatePerson(uuid, person));
        verify(personRepository, times(1)).findById(uuid);
        verify(personRepository, never()).save(any(Person.class));
        verifyNoInteractions(passwordUtil);
    }

    @Test
    void testDeletePerson() {
        // given:
        UUID uuid = UUID.randomUUID();

        // when:
        doNothing().when(personRepository).deleteById(uuid);
        personService.deletePerson(uuid);

        // then:
        verify(personRepository, times(1)).deleteById(uuid);
    }

    @Test
    void testAddPerson_EmailAlreadyExists() {
        // given:
        PersonCreateDTO personDTO = new PersonCreateDTO();
        personDTO.setEmail("existing@example.com");

        // when:
        when(personRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // then:
        ValidationException exception = assertThrows(ValidationException.class,
                () -> personService.addPerson(personDTO));
        assertEquals("Email already exists!", exception.getMessage());
        verify(personRepository, never()).save(any());
    }

    @Test
    void testGetPersonById_Success() {
        // given:
        UUID uuid = UUID.randomUUID();
        Person person = new Person();
        person.setId(uuid);

        // when:
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person));
        Person result = personService.getPersonById(uuid);

        // then:
        assertEquals(uuid, result.getId());
    }

    @Test
    void testGetPersonById_NotFound() {
        // given:
        UUID uuid = UUID.randomUUID();

        // when:
        when(personRepository.findById(uuid)).thenReturn(Optional.empty());

        // then:
        assertThrows(IllegalStateException.class, () -> personService.getPersonById(uuid));
    }

    @Test
    void testGetPersonByEmail_Success() {
        // given:
        String email = "test@example.com";
        Person person = new Person();
        person.setEmail(email);

        // when:
        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        Person result = personService.getPersonByEmail(email);

        // then:
        assertEquals(email, result.getEmail());
    }

    // --- THE COMPLEX FLOW TESTS ---

    @Test
    void testAddGameToLibrary_Success() throws ValidationException {
        // given:
        UUID personId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        Person person = new Person();
        person.setId(personId);
        person.setOwnedGames(new java.util.ArrayList<>());

        com.andrei.demo.model.VideoGame game = new com.andrei.demo.model.VideoGame();
        game.setId(gameId);

        // when:
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(personRepository.save(person)).thenReturn(person);

        Person result = personService.addGameToLibrary(personId, gameId);

        // then:
        assertTrue(result.getOwnedGames().contains(game));
        verify(personRepository, times(1)).save(person);
    }

    @Test
    void testAddGameToLibrary_AlreadyOwned() {
        // given:
        UUID personId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        Person person = new Person();
        person.setId(personId);
        person.setOwnedGames(new java.util.ArrayList<>());

        com.andrei.demo.model.VideoGame game = new com.andrei.demo.model.VideoGame();
        game.setId(gameId);

        // Add the game BEFORE trying to add it again
        person.getOwnedGames().add(game);

        // when:
        when(personRepository.findById(personId)).thenReturn(Optional.of(person));
        when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(game));

        // then:
        ValidationException exception = assertThrows(ValidationException.class,
                () -> personService.addGameToLibrary(personId, gameId));
        assertTrue(exception.getMessage().contains("already owned"));
        verify(personRepository, never()).save(any()); // Ensure save was never called
    }
}