package com.andrei.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.model.Person;
import com.andrei.demo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;


@CrossOrigin
@RestController
@RequestMapping("/person")
@AllArgsConstructor
public class PersonController {
    private final PersonService personService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> getPeople(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Admin privileges required.");
        }

        List<Person> people = personService.getPeople();
        return ResponseEntity.ok(people);
    }

    @GetMapping("{uuid}")
    public ResponseEntity<?> getPersonById(@PathVariable UUID uuid,
                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        if (!"ADMIN".equals(role) && !tokenId.equals(uuid.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only view your own profile.");
        }
        return ResponseEntity.ok(personService.getPersonById(uuid));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getPersonByEmail(@PathVariable String email,
                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        Person person = personService.getPersonByEmail(email);

        if (!"ADMIN".equals(role) && !tokenId.equals(person.getId().toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only view your own profile.");
        }
        return ResponseEntity.ok(person);
    }

    @PostMapping
    public ResponseEntity<?> addPerson(
            @Valid @RequestBody PersonCreateDTO personDTO
    ) throws ValidationException {
        return ResponseEntity.ok(personService.addPerson(personDTO));
    }

    @PostMapping("/{personId}/games/{gameId}")
    public ResponseEntity<?> addGameToLibrary(@PathVariable UUID personId,
                                              @PathVariable UUID gameId,
                                              @RequestHeader("Authorization") String authHeader) throws ValidationException {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        if (!"ADMIN".equals(role) && !tokenId.equals(personId.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only add games to your own library.");
        }
        return ResponseEntity.ok(personService.addGameToLibrary(personId, gameId));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updatePerson(@PathVariable UUID uuid,
                                          @RequestBody Person person,
                                          @RequestHeader("Authorization") String authHeader) throws ValidationException {
        String token = authHeader.substring(7);
        String role = jwtUtil.getRoleFromToken(token);
        String tokenId = jwtUtil.getUserIdFromToken(token);

        if (!"ADMIN".equals(role) && !tokenId.equals(uuid.toString())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: You can only edit your own profile.");
        }
        return ResponseEntity.ok(personService.updatePerson(uuid, person));
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deletePerson(@PathVariable UUID uuid,
                                          @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);

        if (!"ADMIN".equals(jwtUtil.getRoleFromToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admins can delete users.");
        }

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
