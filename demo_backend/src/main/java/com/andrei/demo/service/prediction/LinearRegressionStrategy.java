package com.andrei.demo.service.prediction;

import com.andrei.demo.model.dto.GameAnalyticsDTO.PricePointDTO;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Primary
public class LinearRegressionStrategy implements PricePredictionStrategy {

    @Override
    public List<PricePointDTO> predict(List<PricePointDTO> prices, int daysIntoFuture, int intervalDays) {
        if (prices == null || prices.size() < 2) {
            return new ArrayList<>(); // Not enough data
        }

        int n = prices.size();
        double sumX = 0, sumY = 0, sumXY = 0, sumXX = 0;

        // Use the first date as the base (x=0) to keep numbers manageable
        double x0 = prices.get(0).getDate().toEpochDay();

        for (PricePointDTO p : prices) {
            double x = p.getDate().toEpochDay() - x0;
            double y = p.getPrice();
            sumX  += x;
            sumY  += y;
            sumXY += x * y;
            sumXX += x * x;
        }

        double denom = n * sumXX - sumX * sumX;
        if (denom == 0) return new ArrayList<>(); // Avoid division by zero if all dates are the same

        double slope = (n * sumXY - sumX * sumY) / denom;
        double intercept = (sumY - slope * sumX) / n;

        List<PricePointDTO> predictions = new ArrayList<>();
        var lastHistorical = prices.get(prices.size() - 1);

        // 1) Anchor point so the lines meet
        predictions.add(new PricePointDTO(lastHistorical.getDate(), lastHistorical.getPrice()));

        // 2) Future predictions
        LocalDate lastDate = lastHistorical.getDate();
        for (int i = intervalDays; i <= daysIntoFuture; i += intervalDays) {
            LocalDate futureDate = lastDate.plusDays(i);
            double xFuture = futureDate.toEpochDay() - x0;
            double predicted = slope * xFuture + intercept;

            predicted = Math.max(0, predicted); // Never negative
            predicted = Math.round(predicted * 100.0) / 100.0;

            predictions.add(new PricePointDTO(futureDate, predicted));
        }

        return predictions;
    }
}