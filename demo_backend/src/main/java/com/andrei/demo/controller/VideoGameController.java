package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.VideoGameCreateDTO;
import com.andrei.demo.service.VideoGameService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/videogames")
@AllArgsConstructor
public class VideoGameController {
    private final VideoGameService videoGameService;

    @GetMapping
    public List<VideoGame> getVideoGames() {
        return videoGameService.getAllVideoGames();
    }

    @GetMapping("/{uuid}")
    public VideoGame getVideoGameById(@PathVariable UUID uuid) {
        return videoGameService.getVideoGameById(uuid);
    }

    @GetMapping("/{title}")
    public VideoGame getVideoGameByTitle(@PathVariable String title) {
        return videoGameService.getVideoGameByTitle(title);
    }

    @PostMapping
    public VideoGame addVideoGame(
            @Valid @RequestBody VideoGameCreateDTO videoGameDTO
    ) {
        return videoGameService.addVideoGame(videoGameDTO);
    }

    @PutMapping("/{uuid}")
    public VideoGame updateVideoGame(@PathVariable UUID uuid,
                                     @RequestBody VideoGame videoGame)
            throws ValidationException {
        return videoGameService.updateVideoGame(uuid, videoGame);
    }

    @PatchMapping("/{uuid}")
    public VideoGame patchVideoGame(@PathVariable UUID uuid, @RequestBody Map<String,
                Object> updates) throws ValidationException{
        return videoGameService.patchVideoGame(uuid,updates);
    }

    @DeleteMapping("/{uuid}")
    public void deleteVideoGame(@PathVariable UUID uuid) {
        videoGameService.deleteVideoGame(uuid);
    }
}
