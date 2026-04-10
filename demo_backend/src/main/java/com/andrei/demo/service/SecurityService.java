package com.andrei.demo.service;

import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SecurityService {
    private final PersonRepository personRepository;

    public LoginResponse login(String email, String password) {
        Optional<Person> maybePerson = personRepository.findByEmail(email);
        if(maybePerson.isEmpty()) {
            return new LoginResponse(
                    false,
                    null,
                    "Person with email " + email + " not found"
            );
        }
        Person person = maybePerson.get();
        String role = String.valueOf(person.getRole());
        if(person.getPassword().equals(password)) {
            return new LoginResponse(true, role, null);
        } else {
            return new LoginResponse(false, null, "Incorrect password");
        }
    }
}