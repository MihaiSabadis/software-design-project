package com.andrei.demo.service.prediction;

import com.andrei.demo.model.dto.GameAnalyticsDTO.PricePointDTO;
import java.util.List;

public interface PricePredictionStrategy {
    /**
     * @param historicalPrices The known past prices
     * @param daysIntoFuture How many days ahead to predict (e.g., 180)
     * @param intervalDays The gap between prediction points (e.g., 30)
     * @return A list of predicted future price points
     */
    List<PricePointDTO> predict(List<PricePointDTO> historicalPrices, int daysIntoFuture, int intervalDays);
}