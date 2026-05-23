package com.andrei.demo.service;

import com.andrei.demo.model.GamePatch;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.dto.GamePatchCreateDTO;
import com.andrei.demo.model.dto.GamePatchResponseDTO;
import com.andrei.demo.repository.GamePatchRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.VideoGameRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GamePatchService {

    private final GamePatchRepository gamePatchRepository;
    private final VideoGameRepository videoGameRepository;
    private final PersonRepository personRepository;

    public GamePatchService(GamePatchRepository gamePatchRepository,
                            VideoGameRepository videoGameRepository,
                            PersonRepository personRepository) {
        this.gamePatchRepository = gamePatchRepository;
        this.videoGameRepository = videoGameRepository;
        this.personRepository = personRepository;
    }

    private void assertCanManageGame(VideoGame game) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return;

        boolean isModerator = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR"));
        if (!isModerator) return;

        String principalId = (String) auth.getPrincipal();
        Person moderator = personRepository.findById(UUID.fromString(principalId))
                .orElseThrow(() -> new RuntimeException("Moderator not found."));

        if (moderator.getStudio() == null
                || !moderator.getStudio().getId().equals(game.getStudio().getId())) {
            throw new RuntimeException(
                    "You can only manage patches for your studio's games.");
        }
    }


    @Transactional
    public GamePatchResponseDTO addPatchToGame(UUID gameId, GamePatchCreateDTO patchDTO) {
        VideoGame game = videoGameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException(
                        "Game with ID " + gameId + " not found."));

        assertCanManageGame(game);

        GamePatch patch = new GamePatch();
        patch.setVersion(patchDTO.getVersion());
        patch.setDescription(patchDTO.getDescription());
        patch.setReleaseDate(patchDTO.getReleaseDate());
        patch.setVideoGame(game);

        return mapToDTO(gamePatchRepository.save(patch));
    }

    @Transactional(readOnly = true)
    public List<GamePatchResponseDTO> getPatchesForGame(UUID gameId) {
        return gamePatchRepository
                .findByVideoGameIdOrderByReleaseDateAsc(gameId)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePatch(UUID gameId, UUID patchId) {
        GamePatch patch = gamePatchRepository.findById(patchId)
                .orElseThrow(() -> new RuntimeException("Patch not found."));

        if (!patch.getVideoGame().getId().equals(gameId)) {
            throw new RuntimeException("Patch does not belong to this game.");
        }

        assertCanManageGame(patch.getVideoGame());

        gamePatchRepository.deleteById(patchId);
    }


    private GamePatchResponseDTO mapToDTO(GamePatch patch) {
        GamePatchResponseDTO dto = new GamePatchResponseDTO();
        dto.setId(patch.getId());
        dto.setVersion(patch.getVersion());
        dto.setDescription(patch.getDescription());
        dto.setReleaseDate(patch.getReleaseDate());
        dto.setVideoGameId(patch.getVideoGame().getId());
        return dto;
    }
}