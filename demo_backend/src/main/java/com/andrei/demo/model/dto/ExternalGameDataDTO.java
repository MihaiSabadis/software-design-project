// demo_backend/src/main/java/com/andrei/demo/model/dto/ExternalGameDataDTO.java
package com.andrei.demo.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExternalGameDataDTO {
    private String cheapestPriceEver;
    private Long cheapestPriceDateEpoch;
    private String metacriticScore;
    private String steamRating;
    private List<StoreDealDTO> currentDeals;

    @Data
    public static class StoreDealDTO {
        private String storeName;
        private String price;
        private String retailPrice;
        private String savingsPercent;
        private String dealUrl;
    }
}