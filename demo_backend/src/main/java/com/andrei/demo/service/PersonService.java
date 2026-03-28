package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.VideoGameRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final VideoGameRepository videoGameRepository;

    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    public Person addPerson(PersonCreateDTO personDTO) throws ValidationException {

        if (personRepository.existsByEmail(personDTO.getEmail())) {
            throw new ValidationException("Email already exists!");
        }

        Person person = new Person();

        person.setRole(personDTO.getRole());
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(personDTO.getPassword());

        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) throws ValidationException{
        Optional<Person> personOptional =
                personRepository.findById(uuid);

        if(personOptional.isEmpty()) {
            throw new ValidationException("Person with id " + uuid + " not found");
        }
        Person existingPerson = personOptional.get();

        // check if they are trying to change their email to one that belongs to someone else
        if (!existingPerson.getEmail().equals(person.getEmail()) &&
                personRepository.existsByEmail(person.getEmail())) {
            throw new ValidationException("Email already exists!");
        }

        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        existingPerson.setPassword(person.getPassword());

        return personRepository.save(existingPerson);
    }

    public Person updatePerson2(UUID uuid, Person person) throws ValidationException{
        return personRepository
                        .findById(uuid)
                        .map(existingPerson -> {
                            existingPerson.setName(person.getName());
                            existingPerson.setAge(person.getAge());
                            existingPerson.setEmail(person.getEmail());
                            existingPerson.setPassword(person.getPassword());
                            return personRepository.save(existingPerson);
                        })
                        .orElseThrow(
                                () -> new ValidationException("Person with id " + uuid + " not found")
                        );
    }

    public void deletePerson(UUID uuid) {
        personRepository.deleteById(uuid);
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Person with id " + uuid + " not found"));
    }

    public Person addGameToLibrary(UUID personId, UUID gameId) throws ValidationException {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new ValidationException("Person with ID " + personId + " not found"));

        VideoGame game = videoGameRepository.findById(gameId)
                .orElseThrow(() -> new ValidationException("Game with ID " + gameId + " not found"));

        if(person.getOwnedGames().contains(game)) {
            throw new ValidationException("Game already owned by person " + person);
        }

        person.getOwnedGames().add(game);
        return personRepository.save(person);
    }
}
