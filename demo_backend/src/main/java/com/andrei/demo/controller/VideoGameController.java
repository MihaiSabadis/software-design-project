// demo_backend/src/main/java/com/andrei/demo/controller/VideoGameController.java
package com.andrei.demo.controller;

import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.dto.ExternalGameDataDTO;
import com.andrei.demo.model.dto.GameAnalyticsDTO;
import com.andrei.demo.model.dto.VideoGameCreateDTO;
import com.andrei.demo.service.ExternalGameDataService;
import com.andrei.demo.service.GameAnalyticsService;
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
    private final GameAnalyticsService gameAnalyticsService;

    @GetMapping
    public List<VideoGame> getVideoGames(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String studioName,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (maxPrice == null && studioName == null && title == null) {
            return videoGameService.getAllVideoGames();
        }
        return videoGameService.getFilteredVideoGames(title, studioName, maxPrice, sortBy, sortDir);
    }

    @GetMapping("/{uuid}")
    public VideoGame getVideoGameById(@PathVariable UUID uuid) {
        return videoGameService.getVideoGameById(uuid);
    }

    @GetMapping("/title/{title}")
    public VideoGame getVideoGameByTitle(@PathVariable String title) {
        return videoGameService.getVideoGameByTitle(title);
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PostMapping
    public ResponseEntity<?> addVideoGame(@Valid @RequestBody VideoGameCreateDTO gameDTO) {
        return ResponseEntity.ok(videoGameService.addVideoGame(gameDTO));
    }

    // Change only the PUT endpoint:
    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateVideoGame(@PathVariable UUID uuid,
                                             @Valid @RequestBody VideoGameCreateDTO gameDTO) {
        return ResponseEntity.ok(videoGameService.updateVideoGame(uuid, gameDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PatchMapping("/{uuid}")
    public ResponseEntity<String> patchVideoGame(@PathVariable UUID uuid,
                                                 @RequestBody Map<String, Object> updates) {
        videoGameService.patchVideoGame(uuid, updates);
        return ResponseEntity.ok("Game patched successfully");
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteVideoGame(@PathVariable UUID uuid) {
        videoGameService.deleteVideoGame(uuid);
        return ResponseEntity.ok("Game deleted successfully");
    }

    @GetMapping("/{id}/analytics")
    public ResponseEntity<GameAnalyticsDTO> getGameAnalytics(@PathVariable UUID id) {
        return ResponseEntity.ok(gameAnalyticsService.getAnalyticsForGame(id));
    }

    // Add this injection and endpoint to the existing controller:

    private final ExternalGameDataService externalGameDataService;

    @GetMapping("/{id}/external-data")
    public ResponseEntity<ExternalGameDataDTO> getExternalData(@PathVariable UUID id) {
        return ResponseEntity.ok(externalGameDataService.getExternalData(id));
    }
}