package com.andrei.demo.service; // Change this to your package!

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.ReviewCreateDTO; // (Whatever your DTO is named)
import com.andrei.demo.repository.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTests {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private PersonService personService;

    @Mock
    private VideoGameService videoGameService;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void givenUserDoesNotOwnGame_whenAddReview_thenThrowValidationException() {
        // 1. ARRANGE (Set up our fake data)
        UUID userId = UUID.randomUUID();
        UUID gameId = UUID.randomUUID();

        // Create a fake user with an EMPTY library
        Person fakeUser = new Person();
        fakeUser.setId(userId);
        fakeUser.setOwnedGames(new ArrayList<>()); // User owns NO games

        // Create a fake game
        VideoGame fakeGame = new VideoGame();
        fakeGame.setId(gameId);

        // Tell our mocks what to do when the service calls them
        when(personService.getPersonById(userId)).thenReturn(fakeUser);
        when(videoGameService.getVideoGameById(gameId)).thenReturn(fakeGame);

        // Create the fake payload the user sends from Angular
        ReviewCreateDTO payload = new ReviewCreateDTO(5,"Great game!",userId, gameId);

        // 2. ACT & ASSERT (Check if it throws the error!)
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            reviewService.addReview(payload); // UNCOMMENT THIS when you match your DTO name
        });

        // 3. Verify the message is correct
        assertEquals("User can't review a game that's not in library!", exception.getMessage());
    }
}