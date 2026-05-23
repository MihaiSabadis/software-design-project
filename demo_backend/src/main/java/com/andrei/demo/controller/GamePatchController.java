// demo_backend/src/main/java/com/andrei/demo/controller/GamePatchController.java
package com.andrei.demo.controller;

import com.andrei.demo.model.dto.GamePatchCreateDTO;
import com.andrei.demo.model.dto.GamePatchResponseDTO;
import com.andrei.demo.service.GamePatchService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/videogames/{gameId}/patches")
@AllArgsConstructor
public class GamePatchController {

    private final GamePatchService gamePatchService;

    @GetMapping
    public ResponseEntity<List<GamePatchResponseDTO>> getPatches(@PathVariable UUID gameId) {
        return ResponseEntity.ok(gamePatchService.getPatchesForGame(gameId));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @PostMapping
    public ResponseEntity<GamePatchResponseDTO> addPatch(
            @PathVariable UUID gameId,
            @Valid @RequestBody GamePatchCreateDTO patchDTO) {
        return ResponseEntity.ok(gamePatchService.addPatchToGame(gameId, patchDTO));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MODERATOR')")
    @DeleteMapping("/{patchId}")
    public ResponseEntity<?> deletePatch(
            @PathVariable UUID gameId,
            @PathVariable UUID patchId) {
        gamePatchService.deletePatch(gameId, patchId);
        return ResponseEntity.ok("Patch deleted successfully");
    }
}