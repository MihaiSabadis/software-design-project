package com.andrei.demo.controller;

import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.VideoGameCreateDTO;
import com.andrei.demo.service.VideoGameService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/videogames")
@AllArgsConstructor
public class VideoGameController {

    private final VideoGameService videoGameService;

    @GetMapping
    public List<VideoGame> getVideoGames(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String developer,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (maxPrice == null && developer == null && title == null) {
            return videoGameService.getAllVideoGames();
        } else {
            return videoGameService.getFilteredVideoGames(title, developer, maxPrice, sortBy, sortDir);
        }
    }

    @GetMapping("/{uuid}")
    public VideoGame getVideoGameById(@PathVariable UUID uuid) {
        return videoGameService.getVideoGameById(uuid);
    }

    @GetMapping("/title/{title}")
    public VideoGame getVideoGameByTitle(@PathVariable String title) {
        return videoGameService.getVideoGameByTitle(title);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> addVideoGame(@Valid @RequestBody VideoGameCreateDTO gameDTO) {
        return ResponseEntity.ok(videoGameService.addVideoGame(gameDTO));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateVideoGame(@PathVariable UUID uuid, @RequestBody VideoGame game) {
        return ResponseEntity.ok(videoGameService.updateVideoGame(uuid, game));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{uuid}")
    public ResponseEntity<String> patchVideoGame(@PathVariable UUID uuid, @RequestBody Map<String, Object> updates) {
        videoGameService.patchVideoGame(uuid, updates);
        return ResponseEntity.ok("Game patched successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteVideoGame(@PathVariable UUID uuid) {
        videoGameService.deleteVideoGame(uuid);
        return ResponseEntity.ok("Game deleted successfully");
    }
}