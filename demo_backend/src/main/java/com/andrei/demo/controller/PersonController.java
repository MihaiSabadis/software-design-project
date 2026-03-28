package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.model.Person;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("/person")
@AllArgsConstructor
@CrossOrigin
public class PersonController {
    private final PersonService personService;

    @GetMapping
    public List<Person> getPeople() {
        return personService.getPeople();
    }

    @GetMapping("{uuid}")
    public Person getPersonById(@PathVariable UUID uuid) {
        return personService.getPersonById(uuid);
    }

    @GetMapping("/email/{email}")
    public Person getPersonByEmail(@PathVariable String email) {
        return personService.getPersonByEmail(email);
    }

    @PostMapping
    public Person addPerson(
            @Valid @RequestBody PersonCreateDTO personDTO
    ) throws ValidationException {
        return personService.addPerson(personDTO);
    }

    @PostMapping("/{personId}/games/{gameId}")
    public Person addGameToLibrary(@PathVariable UUID personId, @PathVariable UUID gameId) throws ValidationException {
        return personService.addGameToLibrary(personId, gameId);
    }

    @PutMapping("/{uuid}")
    public Person updatePerson(@PathVariable UUID uuid,
                               @RequestBody Person person)
            throws ValidationException {
        return personService.updatePerson(uuid, person);
    }

    @DeleteMapping("/{uuid}")
    public void deletePerson(@PathVariable UUID uuid) {
        personService.deletePerson(uuid);
    }

}
