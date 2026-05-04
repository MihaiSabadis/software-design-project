package com.andrei.demo.controller;

import org.springframework.http.ResponseEntity;
import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.model.Person;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/person")
@AllArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getPeople() {
        List<Person> people = personService.getPeople();
        return ResponseEntity.ok(people);
    }

    @PreAuthorize("hasRole('ADMIN') or principal == #uuid.toString()")
    @GetMapping("{uuid}")
    public ResponseEntity<?> getPersonById(@PathVariable UUID uuid) {
        return ResponseEntity.ok(personService.getPersonById(uuid));
    }

    // SpEL here: it calls your service to get the ID associated with the email!
    @PreAuthorize("hasRole('ADMIN') or @personService.getPersonByEmail(#email).id.toString() == principal")
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getPersonByEmail(@PathVariable String email) {
        Person person = personService.getPersonByEmail(email);
        return ResponseEntity.ok(person);
    }

    @PostMapping
    public ResponseEntity<?> addPerson(@Valid @RequestBody PersonCreateDTO personDTO) throws ValidationException {
        return ResponseEntity.ok(personService.addPerson(personDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or principal == #personId.toString()")
    @PostMapping("/{personId}/games/{gameId}")
    public ResponseEntity<?> addGameToLibrary(@PathVariable UUID personId, @PathVariable UUID gameId) throws ValidationException {
        return ResponseEntity.ok(personService.addGameToLibrary(personId, gameId));
    }

    @PreAuthorize("hasRole('ADMIN') or principal == #uuid.toString()")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updatePerson(@PathVariable UUID uuid, @RequestBody Person person) throws ValidationException {
        return ResponseEntity.ok(personService.updatePerson(uuid, person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deletePerson(@PathVariable UUID uuid) {
        personService.deletePerson(uuid);
        return ResponseEntity.ok("Person deleted successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            personService.forgotPassword(email);
            return ResponseEntity.ok("If that email exists, a reset code was sent.");
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email,
                                           @RequestParam String code,
                                           @RequestParam String newPassword) {
        try {
            personService.resetPassword(email, code, newPassword);
            return ResponseEntity.ok("Password successfully reset.");
        } catch (ValidationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}