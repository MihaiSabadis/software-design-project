package com.andrei.demo.service.prediction;

import com.andrei.demo.model.dto.GameAnalyticsDTO.PricePointDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class StableBaselineStrategy implements PricePredictionStrategy {

    @Override
    public List<PricePointDTO> predict(List<PricePointDTO> prices, int daysIntoFuture, int intervalDays) {
        if (prices == null || prices.isEmpty()) {
            return new ArrayList<>();
        }

        // average from last 3 points
        int limit = Math.min(3, prices.size());
        double sum = 0;
        for (int i = prices.size() - limit; i < prices.size(); i++) {
            sum += prices.get(i).getPrice();
        }

        double averagePrice = sum / limit;
        averagePrice = Math.round(averagePrice * 100.0) / 100.0;

        List<PricePointDTO> predictions = new ArrayList<>();
        var lastHistorical = prices.getLast();

        // start last price point
        predictions.add(new PricePointDTO(lastHistorical.getDate(), lastHistorical.getPrice()));

        // future predictions (flat line of the recent average)
        LocalDate lastDate = lastHistorical.getDate();
        for (int i = intervalDays; i <= daysIntoFuture; i += intervalDays) {
            LocalDate futureDate = lastDate.plusDays(i);
            predictions.add(new PricePointDTO(futureDate, averagePrice));
        }

        return predictions;
    }
}