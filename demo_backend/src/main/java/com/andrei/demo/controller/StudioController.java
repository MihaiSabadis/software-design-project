package com.andrei.demo.controller;

import com.andrei.demo.model.Studio;
import com.andrei.demo.model.dto.StudioCreateDTO;
import com.andrei.demo.service.StudioService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/studios")
@AllArgsConstructor
public class StudioController {

    private final StudioService studioService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<Studio>> getAllStudios() {
        return ResponseEntity.ok(studioService.getAllStudios());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<Studio> createStudio(@Valid @RequestBody StudioCreateDTO dto) {
        return ResponseEntity.ok(studioService.createStudio(dto.getName(), dto.getDescription()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteStudio(@PathVariable UUID id) {
        studioService.deleteStudio(id);
        return ResponseEntity.ok("Studio deleted successfully");
    }
}