package com.andrei.demo.controller;

import com.andrei.demo.model.LoginRequest;
import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.dto.PersonCreateDTO;
import com.andrei.demo.service.PersonService;
import com.andrei.demo.service.SecurityService;
import jakarta.validation.Valid;
import com.andrei.demo.config.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@AllArgsConstructor
public class LoginController {
    private final SecurityService securityService;
    private final PersonService personService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = securityService.login(loginRequest.email(), loginRequest.password());
        if(loginResponse.success()) {
            return ResponseEntity.ok(loginResponse);
        } else {
            return ResponseEntity.status(UNAUTHORIZED).body(loginResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid PersonCreateDTO personCreateDTO){
        try{
            personService.addPerson(personCreateDTO);
            return ResponseEntity.ok("Account created successfully!");

        }catch (ValidationException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}