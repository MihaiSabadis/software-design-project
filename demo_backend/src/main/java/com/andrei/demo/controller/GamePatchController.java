package com.andrei.demo.controller;

import com.andrei.demo.model.dto.GamePatchCreateDTO;
import com.andrei.demo.model.dto.GamePatchResponseDTO;
import com.andrei.demo.service.GamePatchService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/videogames/{gameId}/patches")
public class GamePatchController {

    private final GamePatchService gamePatchService;

    public GamePatchController(GamePatchService gamePatchService) {
        this.gamePatchService = gamePatchService;
    }

    @GetMapping
    public ResponseEntity<List<GamePatchResponseDTO>> getPatches(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gamePatchService.getPatchesForGame(gameId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PostMapping
    public ResponseEntity<GamePatchResponseDTO> addPatch(
            @PathVariable UUID gameId,
            @Valid @RequestBody GamePatchCreateDTO patchDTO) {

        GamePatchResponseDTO createdPatch = gamePatchService.addPatchToGame(gameId, patchDTO);
        return ResponseEntity.ok(createdPatch);
    }
}