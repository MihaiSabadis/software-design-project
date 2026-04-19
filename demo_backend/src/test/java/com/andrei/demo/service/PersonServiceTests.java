package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.repository.PersonRepository;
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

class PersonServiceTests {

    @Mock
    private PersonRepository personRepository;

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

        Person savedPerson = new Person();
        savedPerson.setId(UUID.randomUUID());
        savedPerson.setName("John");
        savedPerson.setAge(30);
        savedPerson.setEmail("john@example.com");
        savedPerson.setPassword("password");

        // when:
        when(personRepository.save(any(Person.class))).thenReturn(savedPerson);
        Person result = personService.addPerson(personDTO);

        // then:
        assertEquals(savedPerson, result);
        assertNotNull(result.getId());
        verify(personRepository, times(1)).save(any(Person.class));
    }

    @Test
    void testUpdatePerson() throws ValidationException {
        // given:
        UUID uuid = UUID.randomUUID();
        Person person = new Person();
        person.setId(uuid);
        person.setName("John");
        person.setAge(30);
        person.setEmail("john@example.com");
        person.setPassword("password");

        Person updatedPerson = new Person();
        updatedPerson.setId(uuid);
        updatedPerson.setName("Jane");
        updatedPerson.setAge(25);
        updatedPerson.setEmail("jane@example.com");
        updatedPerson.setPassword("newpassword");

        // when:
        when(personRepository.findById(uuid)).thenReturn(Optional.of(person));
        when(personRepository.save(any())).thenReturn(updatedPerson);
        Person result = personService.updatePerson(uuid, updatedPerson);

        // then:
        assertEquals("Jane", result.getName());
        verify(personRepository, times(1)).findById(uuid);
        verify(personRepository, times(1)).save(updatedPerson);
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
}