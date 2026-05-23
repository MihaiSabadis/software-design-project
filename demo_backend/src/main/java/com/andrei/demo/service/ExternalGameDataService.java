package com.andrei.demo.service;

import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.dto.ExternalGameDataDTO;
import com.andrei.demo.repository.VideoGameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExternalGameDataService {

    private final CheapSharkService cheapSharkService;
    private final VideoGameRepository videoGameRepository;

    public ExternalGameDataDTO getExternalData(UUID gameId) {
        VideoGame game = videoGameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        ExternalGameDataDTO dto = new ExternalGameDataDTO();
        String title = game.getTitle();

        cheapSharkService.findCheapSharkGameId(title)
                .ifPresent(csId -> cheapSharkService.enrichWithCheapSharkData(csId, dto));

        return dto;
    }
}