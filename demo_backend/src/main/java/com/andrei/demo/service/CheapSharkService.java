package com.andrei.demo.service;

import com.andrei.demo.model.dto.ExternalGameDataDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheapSharkService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    private static final String BASE_URL = "https://www.cheapshark.com/api/1.0";
    private static final String DEAL_URL = "https://www.cheapshark.com/redirect?dealID=";

    // Store ID → Name mapping (CheapShark uses numeric IDs)
    private static final java.util.Map<String, String> STORE_NAMES = java.util.Map.of(
            "1", "Steam",
            "7", "GOG",
            "11", "Humble Store",
            "13", "Fanatical",
            "15", "Gamesplanet"
    );

    public Optional<String> findCheapSharkGameId(String title) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(BASE_URL + "/games")
                    .queryParam("title", title)
                    .queryParam("limit", 3)
                    .toUriString();

            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode response = objectMapper.readTree(jsonResponse);

            if (response != null && response.isArray() && !response.isEmpty()) {
                return Optional.of(response.get(0).get("gameID").asText());
            }
        } catch (Exception e) {
            log.warn("CheapShark search failed for title '{}': {}", title, e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<String> findSteamAppId(String title) {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(BASE_URL + "/games")
                    .queryParam("title", title)
                    .queryParam("limit", 3)
                    .toUriString();

            // FIXED: Fetch as String first
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode response = objectMapper.readTree(jsonResponse);

            if (response != null && response.isArray() && !response.isEmpty()) {
                JsonNode first = response.get(0);
                if (first.has("steamAppID") && !first.get("steamAppID").isNull()) {
                    return Optional.of(first.get("steamAppID").asText());
                }
            }
        } catch (Exception e) {
            log.warn("CheapShark steamAppID lookup failed for '{}': {}", title, e.getMessage());
        }
        return Optional.empty();
    }

    public void enrichWithCheapSharkData(String cheapSharkId, ExternalGameDataDTO dto) {
        try {
            String url = BASE_URL + "/games?id=" + cheapSharkId;

            // FIXED: Fetch as String first
            String jsonResponse = restTemplate.getForObject(url, String.class);
            JsonNode response = objectMapper.readTree(jsonResponse);

            if (response == null) return;

            // Cheapest price ever
            JsonNode cheapest = response.get("cheapestPriceEver");
            if (cheapest != null) {
                dto.setCheapestPriceEver(cheapest.get("price").asText());
                dto.setCheapestPriceDateEpoch(cheapest.get("date").asLong());
            }

            // Metacritic + Steam rating
            JsonNode info = response.get("info");
            if (info != null) {
                if (info.has("metacriticScore"))
                    dto.setMetacriticScore(info.get("metacriticScore").asText());
                if (info.has("steamRatingText"))
                    dto.setSteamRating(info.get("steamRatingText").asText());
            }

            // Current deals across stores
            JsonNode deals = response.get("deals");
            if (deals != null && deals.isArray()) {
                List<ExternalGameDataDTO.StoreDealDTO> dealList = new ArrayList<>();
                for (JsonNode deal : deals) {
                    ExternalGameDataDTO.StoreDealDTO storeDeal = new ExternalGameDataDTO.StoreDealDTO();
                    String storeId = deal.get("storeID").asText();
                    storeDeal.setStoreName(STORE_NAMES.getOrDefault(storeId, "Store " + storeId));
                    storeDeal.setPrice(deal.get("price").asText());
                    storeDeal.setRetailPrice(deal.get("retailPrice").asText());
                    storeDeal.setSavingsPercent(
                            String.format("%.0f", deal.get("savings").asDouble())
                    );
                    storeDeal.setDealUrl(DEAL_URL + deal.get("dealID").asText());
                    dealList.add(storeDeal);
                }
                dto.setCurrentDeals(dealList);
            }

        } catch (Exception e) {
            log.warn("CheapShark enrichment failed for id '{}': {}", cheapSharkId, e.getMessage());
        }
    }
}