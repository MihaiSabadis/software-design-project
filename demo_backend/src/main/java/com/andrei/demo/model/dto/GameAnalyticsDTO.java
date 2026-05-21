package com.andrei.demo.model.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class GameAnalyticsDTO {
    private List<PricePointDTO> priceHistory;
    private List<PatchPointDTO> patchHistory;

    private List<PricePointDTO> linearPrediction;
    private List<PricePointDTO> baselinePrediction;

    @Data
    public static class PricePointDTO {
        private LocalDate date;
        private Double price;

        public PricePointDTO(LocalDate date, Double price) {
            this.date = date;
            this.price = price;
        }
    }

    @Data
    public static class PatchPointDTO {
        private LocalDate date;
        private String version;
        private String description;

        public PatchPointDTO(LocalDate date, String version, String description) {
            this.date = date;
            this.version = version;
            this.description = description;
        }
    }
}