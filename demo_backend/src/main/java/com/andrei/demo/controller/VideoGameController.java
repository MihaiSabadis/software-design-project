package com.andrei.demo.controller;

import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.VideoGameCreateDTO;
import com.andrei.demo.service.VideoGameService;
import com.andrei.demo.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final JwtUtil jwtUtil;

    @GetMapping
    public List<VideoGame> getVideoGames(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String developer,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (maxPrice == null && developer == null && title == null) {
            return videoGameService.getAllVideoGames();
        }
        else{
            return videoGameService.getFilteredVideoGames(title,developer,maxPrice,sortBy,sortDir);
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

    @PostMapping
    public ResponseEntity<?> addVideoGame(@Valid @RequestBody VideoGameCreateDTO gameDTO,
                                          @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!"ADMIN".equals(jwtUtil.getRoleFromToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admins can create games.");
        }
        return ResponseEntity.ok(videoGameService.addVideoGame(gameDTO));
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<?> updateVideoGame(@PathVariable UUID uuid,
                                             @RequestBody VideoGame game,
                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!"ADMIN".equals(jwtUtil.getRoleFromToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admins can edit games.");
        }
        return ResponseEntity.ok(videoGameService.updateVideoGame(uuid, game));
    }

    @PatchMapping("/{uuid}")
    public ResponseEntity<String> patchVideoGame(@PathVariable UUID uuid, @RequestBody Map<String,
                Object> updates, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!"ADMIN".equals(jwtUtil.getRoleFromToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admins can delete games.");
        }
        videoGameService.patchVideoGame(uuid,updates);
        return ResponseEntity.ok("Game patched successfully");
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<?> deleteVideoGame(@PathVariable UUID uuid,
                                             @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        if (!"ADMIN".equals(jwtUtil.getRoleFromToken(token))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access Denied: Only Admins can delete games.");
        }
        videoGameService.deleteVideoGame(uuid);
        return ResponseEntity.ok("Game deleted successfully");
    }
}
