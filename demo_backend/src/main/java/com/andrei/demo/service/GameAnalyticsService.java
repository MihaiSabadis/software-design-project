package com.andrei.demo.service;

import com.andrei.demo.model.dto.GameAnalyticsDTO;
import com.andrei.demo.repository.GamePatchRepository;
import com.andrei.demo.repository.PriceHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameAnalyticsService {

    private final PriceHistoryRepository priceHistoryRepository;
    private final GamePatchRepository gamePatchRepository;

    public GameAnalyticsService(PriceHistoryRepository priceHistoryRepository, GamePatchRepository gamePatchRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.gamePatchRepository = gamePatchRepository;
    }

    @Transactional(readOnly = true)
    public GameAnalyticsDTO getAnalyticsForGame(UUID gameId) {
        GameAnalyticsDTO analytics = new GameAnalyticsDTO();

        // 1. Extragem și mapăm istoricul de prețuri
        var prices = priceHistoryRepository.findByVideoGameIdOrderByRecordedAtAsc(gameId)
                .stream()
                .map(ph -> new GameAnalyticsDTO.PricePointDTO(ph.getRecordedAt(), ph.getPrice()))
                .collect(Collectors.toList());

        // 2. Extragem și mapăm istoricul de patch-uri
        var patches = gamePatchRepository.findByVideoGameIdOrderByReleaseDateAsc(gameId) // Atentie sa faci metoda ASC in repo!
                .stream()
                .map(gp -> new GameAnalyticsDTO.PatchPointDTO(gp.getReleaseDate(), gp.getVersion(), gp.getDescription()))
                .collect(Collectors.toList());

        analytics.setPriceHistory(prices);
        analytics.setPatchHistory(patches);

        return analytics;
    }
}