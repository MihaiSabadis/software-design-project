package com.andrei.demo.service;

import com.andrei.demo.model.dto.GameAnalyticsDTO;
import com.andrei.demo.repository.GamePatchRepository;
import com.andrei.demo.repository.PriceHistoryRepository;
import com.andrei.demo.service.prediction.PricePredictionStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameAnalyticsService {

    private static final int PREDICTION_HORIZON_DAYS = 360;
    private static final int PREDICTION_STEP_DAYS    = 60;

    private final PriceHistoryRepository priceHistoryRepository;
    private final GamePatchRepository gamePatchRepository;
    private final PricePredictionStrategy linearStrategy;
    private final PricePredictionStrategy baselineStrategy;

    public GameAnalyticsService(
            PriceHistoryRepository priceHistoryRepository,
            GamePatchRepository gamePatchRepository,
            @Qualifier("linearRegressionStrategy") PricePredictionStrategy linearStrategy,
            @Qualifier("stableBaselineStrategy") PricePredictionStrategy baselineStrategy) {

        this.priceHistoryRepository = priceHistoryRepository;
        this.gamePatchRepository = gamePatchRepository;
        this.linearStrategy = linearStrategy;
        this.baselineStrategy = baselineStrategy;
    }

    @Transactional(readOnly = true)
    public GameAnalyticsDTO getAnalyticsForGame(UUID gameId) {
        GameAnalyticsDTO analytics = new GameAnalyticsDTO();

        var prices = priceHistoryRepository.findByVideoGameIdOrderByRecordedAtAsc(gameId)
                .stream()
                .map(ph -> new GameAnalyticsDTO.PricePointDTO(ph.getRecordedAt(), ph.getPrice()))
                .collect(Collectors.toList());

        var patches = gamePatchRepository.findByVideoGameIdOrderByReleaseDateAsc(gameId)
                .stream()
                .map(gp -> new GameAnalyticsDTO.PatchPointDTO(gp.getReleaseDate(), gp.getVersion(), gp.getDescription()))
                .collect(Collectors.toList());

        analytics.setPriceHistory(prices);
        analytics.setPatchHistory(patches);

        analytics.setLinearPrediction(linearStrategy.predict(prices, PREDICTION_HORIZON_DAYS, PREDICTION_STEP_DAYS));
        analytics.setBaselinePrediction(baselineStrategy.predict(prices, PREDICTION_HORIZON_DAYS, PREDICTION_STEP_DAYS));
        return analytics;
    }
}