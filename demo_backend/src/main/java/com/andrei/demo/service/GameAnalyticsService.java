package com.andrei.demo.service;

import com.andrei.demo.model.dto.GameAnalyticsDTO;
import com.andrei.demo.repository.GamePatchRepository;
import com.andrei.demo.repository.PriceHistoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GameAnalyticsService {

    private static final int PREDICTION_HORIZON_DAYS = 360;
    private static final int PREDICTION_STEP_DAYS    = 60;

    private final PriceHistoryRepository priceHistoryRepository;
    private final GamePatchRepository gamePatchRepository;

    public GameAnalyticsService(PriceHistoryRepository priceHistoryRepository,
                                GamePatchRepository gamePatchRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.gamePatchRepository = gamePatchRepository;
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
                .map(gp -> new GameAnalyticsDTO.PatchPointDTO(
                        gp.getReleaseDate(), gp.getVersion(), gp.getDescription()))
                .collect(Collectors.toList());

        analytics.setPriceHistory(prices);
        analytics.setPatchHistory(patches);

        if (prices.size() > 1) {
            analytics.setPricePrediction(predictFuturePrices(prices));
        }

        return analytics;
    }

    /**
     * Simple ordinary-least-squares linear regression over the price history,
     * then extrapolated PREDICTION_HORIZON_DAYS into the future.
     *
     * The returned list's FIRST point is the actual last historical price (same
     * date, same price). This makes the prediction line on the chart start
     * exactly where the history line ends, so they connect visually instead
     * of jumping to the regression's value at that date.
     */
    private List<GameAnalyticsDTO.PricePointDTO> predictFuturePrices(
            List<GameAnalyticsDTO.PricePointDTO> prices) {

        // Use days-since-first-observation as x. Subtracting the offset keeps
        // the numbers small and avoids precision loss when squaring epoch days
        // (which are ~20k today — squared, that's 4e8, harmless, but it's still
        // a clean habit).
        final long x0 = prices.get(0).getDate().toEpochDay();
        final int  n  = prices.size();

        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;
        for (var p : prices) {
            double x = p.getDate().toEpochDay() - x0;
            double y = p.getPrice();
            sumX  += x;
            sumY  += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denom = n * sumXX - sumX * sumX;
        if (denom == 0) return List.of(); // all observations on the same day — nothing to predict

        double slope     = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        // 1) Anchor: copy the actual last historical point so the lines meet.
        var lastHistorical = prices.get(prices.size() - 1);
        List<GameAnalyticsDTO.PricePointDTO> predictions = new ArrayList<>();
        predictions.add(new GameAnalyticsDTO.PricePointDTO(
                lastHistorical.getDate(),
                lastHistorical.getPrice()));

        // 2) Future predictions at fixed intervals.
        LocalDate lastDate = lastHistorical.getDate();
        for (int i = PREDICTION_STEP_DAYS; i <= PREDICTION_HORIZON_DAYS; i += PREDICTION_STEP_DAYS) {
            LocalDate futureDate = lastDate.plusDays(i);
            double xFuture = futureDate.toEpochDay() - x0;
            double predicted = slope * xFuture + intercept;

            predicted = Math.max(0, predicted);                       // never negative
            predicted = Math.round(predicted * 100.0) / 100.0;        // 2 decimals

            predictions.add(new GameAnalyticsDTO.PricePointDTO(futureDate, predicted));
        }

        return predictions;
    }
}