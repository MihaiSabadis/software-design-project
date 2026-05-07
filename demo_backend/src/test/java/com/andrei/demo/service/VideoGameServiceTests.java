package com.andrei.demo.service;

import jakarta.validation.ValidationException;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.dto.VideoGameCreateDTO;
import com.andrei.demo.repository.VideoGameRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VideoGameServiceTests {

    @Mock
    private VideoGameRepository videoGameRepository;

    @InjectMocks
    private VideoGameService videoGameService;

    private AutoCloseable closeable;

    private VideoGame game;
    private UUID gameId;
    private VideoGameCreateDTO gameDTO;

    @BeforeEach
    void setUp() {

        closeable = MockitoAnnotations.openMocks(this);

        gameId = UUID.randomUUID();

        game = new VideoGame();
        game.setId(gameId);
        game.setTitle("The Witcher 3");
        game.setPrice(39.99);

        gameDTO = new VideoGameCreateDTO();
        gameDTO.setTitle("The Witcher 3");
        gameDTO.setPrice(39.99);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testGetVideoGames() {
        when(videoGameRepository.findAll()).thenReturn(List.of(game));
        List<VideoGame> result = videoGameService.getAllVideoGames();
        assertEquals(1, result.size());
    }

    @Test
    void testGetVideoGameById_Success() {
        when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(game));
        VideoGame result = videoGameService.getVideoGameById(gameId);
        assertEquals(gameId, result.getId());
    }

    @Test
    void testGetVideoGameById_NotFound() {
        when(videoGameRepository.findById(gameId)).thenReturn(Optional.empty());
        assertThrows(ValidationException.class, () -> videoGameService.getVideoGameById(gameId));
    }

    @Test
    void testAddVideoGame_Success() throws ValidationException {
        when(videoGameRepository.existsByTitle(gameDTO.getTitle())).thenReturn(false);
        when(videoGameRepository.save(any(VideoGame.class))).thenReturn(game);

        VideoGame result = videoGameService.addVideoGame(gameDTO);
        assertNotNull(result);
        assertEquals("The Witcher 3", result.getTitle());
    }

    @Test
    void testAddVideoGame_DuplicateTitle() {
        when(videoGameRepository.existsByTitle(gameDTO.getTitle())).thenReturn(true);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> videoGameService.addVideoGame(gameDTO));

        assertTrue(exception.getMessage().toLowerCase().contains("game with this title already exists"));
        verify(videoGameRepository, never()).save(any());
    }

    @Test
    void testUpdateVideoGame_Success() throws ValidationException {
        VideoGame updatedGame = new VideoGame();
        updatedGame.setTitle("The Witcher 3: Wild Hunt");
        updatedGame.setPrice(19.99);

        when(videoGameRepository.findById(gameId)).thenReturn(Optional.of(game));
        when(videoGameRepository.save(any(VideoGame.class))).thenReturn(updatedGame);

        VideoGame result = videoGameService.updateVideoGame(gameId, updatedGame);

        assertEquals("The Witcher 3: Wild Hunt", result.getTitle());
        assertEquals(19.99, result.getPrice());
    }

    @Test
    void testUpdateVideoGame_NotFound() {
        when(videoGameRepository.findById(gameId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class,
                () -> videoGameService.updateVideoGame(gameId, new VideoGame()));
    }

    @Test
    void testDeleteVideoGame_Success() throws ValidationException {

        when(videoGameRepository.existsById(gameId)).thenReturn(true);

        doNothing().when(videoGameRepository).deleteById(gameId);

        videoGameService.deleteVideoGame(gameId);

        verify(videoGameRepository, times(1)).deleteById(gameId);
    }

    @Test
    void testDeleteVideoGame_NotFound() {

        when(videoGameRepository.existsById(gameId)).thenReturn(false);

        ValidationException exception = assertThrows(ValidationException.class,
                () -> videoGameService.deleteVideoGame(gameId));

        assertTrue(exception.getMessage().contains("Cannot delete"));

        verify(videoGameRepository, never()).deleteById(any());
    }
}

