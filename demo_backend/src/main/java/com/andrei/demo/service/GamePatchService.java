package com.andrei.demo.service;

import com.andrei.demo.model.GamePatch;
import com.andrei.demo.model.dto.GamePatchCreateDTO;
import com.andrei.demo.model.dto.GamePatchResponseDTO;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.repository.GamePatchRepository;
import com.andrei.demo.repository.VideoGameRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GamePatchService {

    private final GamePatchRepository gamePatchRepository;
    private final VideoGameRepository videoGameRepository;

    public GamePatchService(GamePatchRepository gamePatchRepository, VideoGameRepository videoGameRepository) {
        this.gamePatchRepository = gamePatchRepository;
        this.videoGameRepository = videoGameRepository;
    }

    @Transactional
    public GamePatchResponseDTO addPatchToGame(UUID gameId, GamePatchCreateDTO patchDTO) {
        // 1. Verificăm dacă jocul există
        VideoGame game = videoGameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Jocul cu ID-ul " + gameId + " nu a fost găsit!"));

        // 2. Creăm entitatea de bază din DTO
        GamePatch patch = new GamePatch();
        patch.setVersion(patchDTO.getVersion());
        patch.setDescription(patchDTO.getDescription());
        patch.setReleaseDate(patchDTO.getReleaseDate());

        // 3. Facem legătura!
        patch.setVideoGame(game);

        // 4. Salvăm în baza de date
        GamePatch savedPatch = gamePatchRepository.save(patch);

        return mapToResponseDTO(savedPatch);
    }

    @Transactional(readOnly = true)
    public List<GamePatchResponseDTO> getPatchesForGame(UUID gameId) {
        return gamePatchRepository.findByVideoGameIdOrderByReleaseDateAsc(gameId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Funcție utilitară pentru mapare
    private GamePatchResponseDTO mapToResponseDTO(GamePatch patch) {
        GamePatchResponseDTO dto = new GamePatchResponseDTO();
        dto.setId(patch.getId());
        dto.setVersion(patch.getVersion());
        dto.setDescription(patch.getDescription());
        dto.setReleaseDate(patch.getReleaseDate());
        dto.setVideoGameId(patch.getVideoGame().getId());
        return dto;
    }
}