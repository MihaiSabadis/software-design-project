package com.andrei.demo.service;

import com.andrei.demo.model.dto.ExternalGameDataDTO;
import com.andrei.demo.model.dto.StoreDealDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@Service
public class CheapSharkService {

    private static final Logger log = LoggerFactory.getLogger(CheapSharkService.class);
    private static final String BASE_URL = "https://www.cheapshark.com/api/1.0";

    private static final Map<String, String> STORE_NAMES = Map.of(
            "1", "Steam",
            "7", "GOG",
            "11", "Humble Store",
            "13", "Fanatical",
            "15", "Gamesplanet",
            "25", "Epic Games Store",
            "3", "GreenManGaming",
            "2", "GamersGate",
            "8", "Origin",
            "30", "IndieGala"
    );

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public CheapSharkService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    // ──────────────────────────────────────────────────────────────
    // Startup self-test — fires once on boot so you can see at a glance
    // whether the backend can reach CheapShark at all.
    // ──────────────────────────────────────────────────────────────
    @PostConstruct
    public void selfTest() {
        log.info("[CheapShark] Running connectivity self-test against {}", BASE_URL);
        try {
            URI testUri = UriComponentsBuilder
                    .fromUriString(BASE_URL + "/games")
                    .queryParam("title", "portal")
                    .queryParam("limit", 1)
                    .build()
                    .encode()
                    .toUri();
            ResponseEntity<String> resp = restTemplate.getForEntity(testUri, String.class);
            String body = resp.getBody();
            log.info("[CheapShark] Self-test OK — status={}, body[0..120]={}",
                    resp.getStatusCode(),
                    body == null ? "<null>" : body.substring(0, Math.min(120, body.length())));
        } catch (Exception e) {
            log.error("[CheapShark] Self-test FAILED — {}: {} (cause: {})",
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e.getCause() == null ? "<none>" : e.getCause().toString(),
                    e);
        }
    }

    public Optional<String> findCheapSharkGameId(String title) {
        if (title == null || title.isBlank()) {
            log.warn("[CheapShark] findCheapSharkGameId called with blank title — skipping.");
            return Optional.empty();
        }

        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/games")
                .queryParam("title", title)
                .queryParam("limit", 3)
                .build()
                .encode()
                .toUri();

        log.info("[CheapShark] GET {}", uri);

        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            HttpStatusCode status = resp.getStatusCode();
            String body = resp.getBody();

            log.info("[CheapShark] response status={}, length={}",
                    status, body == null ? 0 : body.length());

            if (!status.is2xxSuccessful() || body == null || body.isBlank()) {
                log.warn("[CheapShark] Non-success or empty response — body preview: {}",
                        body == null ? "<null>" : body.substring(0, Math.min(200, body.length())));
                return Optional.empty();
            }

            JsonNode response = objectMapper.readTree(body);
            if (!response.isArray() || response.isEmpty()) {
                log.info("[CheapShark] No matches in catalog for title '{}'", title);
                return Optional.empty();
            }

            JsonNode first = response.get(0);
            JsonNode idNode = first.get("gameID");
            if (idNode == null || idNode.isNull()) {
                log.warn("[CheapShark] First match missing 'gameID' field — payload: {}", first);
                return Optional.empty();
            }

            String gameId = idNode.asText();
            log.info("[CheapShark] Matched '{}' → gameID={} (external title='{}')",
                    title, gameId,
                    first.get("external") == null ? "?" : first.get("external").asText());
            return Optional.of(gameId);

        } catch (RestClientException e) {
            log.error("[CheapShark] HTTP call failed for title='{}' — {}: {} (cause: {})",
                    title,
                    e.getClass().getSimpleName(),
                    e.getMessage(),
                    e.getCause() == null ? "<none>" : e.getCause().toString(),
                    e);
            return Optional.empty();
        } catch (Exception e) {
            log.error("[CheapShark] Unexpected error for title='{}' — {}: {}",
                    title, e.getClass().getSimpleName(), e.getMessage(), e);
            return Optional.empty();
        }
    }

    public Optional<String> findPortraitCoverUrl(String title) {
        return findCheapSharkGameId(title).flatMap(this::resolveSteamCoverUrl);
    }

    private Optional<String> resolveSteamCoverUrl(String csGameId) {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/games")
                .queryParam("id", csGameId)
                .build()
                .encode()
                .toUri();

        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(resp.getBody());
            String steamAppId = root.path("info").path("steamAppID").asText(null);

            if (steamAppId == null || steamAppId.isBlank()) {
                log.info("[CheapShark] No steamAppID for csGameId={}", csGameId);
                return Optional.empty();
            }

            String url = "https://cdn.cloudflare.steamstatic.com/steam/apps/"
                    + steamAppId + "/library_600x900.jpg";
            log.info("[CheapShark] Resolved portrait cover URL: {}", url);
            return Optional.of(url);

        } catch (Exception e) {
            log.warn("[CheapShark] Portrait lookup failed for csGameId={} — {}: {}",
                    csGameId, e.getClass().getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    public void enrichWithCheapSharkData(String csGameId, ExternalGameDataDTO dto) {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/games")
                .queryParam("id", csGameId)
                .build()
                .encode()
                .toUri();

        log.info("[CheapShark] GET {}", uri);

        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            HttpStatusCode status = resp.getStatusCode();
            String body = resp.getBody();

            log.info("[CheapShark] enrich response status={}, length={}",
                    status, body == null ? 0 : body.length());

            if (!status.is2xxSuccessful() || body == null || body.isBlank()) {
                log.warn("[CheapShark] enrich failed — empty/non-2xx body");
                return;
            }

            JsonNode root = objectMapper.readTree(body);

            JsonNode info = root.path("info");
            JsonNode cheapest = root.path("cheapestPriceEver");
            JsonNode deals = root.path("deals");

            if (!cheapest.isMissingNode()) {
                String price = cheapest.path("price").asText(null);
                long epoch = cheapest.path("date").asLong(0L);
                dto.setCheapestPriceEver(price);
                if (epoch > 0) dto.setCheapestPriceDateEpoch(epoch);
            }

            if (deals.isArray() && !deals.isEmpty()) {
                List<StoreDealDTO> mapped = new ArrayList<>();
                for (JsonNode d : deals) {
                    String storeId = d.path("storeID").asText();
                    String storeName = STORE_NAMES.get(storeId);
                    if (storeName == null) continue; // skip stores we don't display

                    String dealId = d.path("dealID").asText(null);
                    if (dealId == null) continue;

                    StoreDealDTO sd = new StoreDealDTO();
                    sd.setStoreName(storeName);
                    sd.setPrice(d.path("price").asText());
                    sd.setRetailPrice(d.path("retailPrice").asText());
                    sd.setSavingsPercent(d.path("savings").asText("0"));
                    sd.setDealUrl("https://www.cheapshark.com/redirect?dealID=" + dealId);
                    mapped.add(sd);
                }
                dto.setCurrentDeals(mapped);
                log.info("[CheapShark] Mapped {} deals (from {} returned by API)",
                        mapped.size(), deals.size());
            }

            if (deals.isArray() && !deals.isEmpty()) {
                String firstDealId = deals.get(0).path("dealID").asText(null);
                if (firstDealId != null) {
                    enrichReviewScores(firstDealId, dto);
                }
            }

        } catch (Exception e) {
            log.error("[CheapShark] enrich failed for csGameId={} — {}: {}",
                    csGameId, e.getClass().getSimpleName(), e.getMessage(), e);
        }
    }

    private void enrichReviewScores(String dealId, ExternalGameDataDTO dto) {
        URI uri = UriComponentsBuilder
                .fromUriString(BASE_URL + "/deals")
                .queryParam("id", dealId)
                .build()
                .encode()
                .toUri();

        log.info("[CheapShark] GET {}", uri);

        try {
            ResponseEntity<String> resp = restTemplate.getForEntity(uri, String.class);
            String body = resp.getBody();
            if (body == null || body.isBlank()) return;

            JsonNode root = objectMapper.readTree(body);
            JsonNode gameInfo = root.path("gameInfo");

            String meta = gameInfo.path("metacriticScore").asText(null);
            String steam = gameInfo.path("steamRatingText").asText(null);

            if (meta != null && !meta.isBlank() && !"0".equals(meta)) {
                dto.setMetacriticScore(meta);
            }
            if (steam != null && !steam.isBlank()) {
                dto.setSteamRating(steam);
            }

            log.info("[CheapShark] review scores → metacritic={}, steam={}", meta, steam);
        } catch (Exception e) {
            log.warn("[CheapShark] review-score enrichment failed for dealId={} — {}: {}",
                    dealId, e.getClass().getSimpleName(), e.getMessage());
        }
    }
}