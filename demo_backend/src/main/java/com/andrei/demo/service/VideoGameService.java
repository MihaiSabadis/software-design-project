package com.andrei.demo.service;

import com.andrei.demo.model.Person;
import com.andrei.demo.model.Studio;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.dto.VideoGameCreateDTO;
import com.andrei.demo.model.PriceHistory;
import com.andrei.demo.repository.PriceHistoryRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.repository.StudioRepository;
import com.andrei.demo.repository.VideoGameRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.time.LocalDate;

@Service
@AllArgsConstructor
public class VideoGameService {

    private final VideoGameRepository videoGameRepository;
    private final StudioRepository studioRepository;
    private final PersonRepository personRepository;
    private final PriceHistoryRepository priceHistoryRepository;

    private Person getModeratorIfApplicable() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;

        boolean isModerator = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR"));
        if (!isModerator) return null;

        String principalId = (String) auth.getPrincipal();
        return personRepository.findById(UUID.fromString(principalId))
                .orElseThrow(() -> new ValidationException("Moderator not found."));
    }

    private void assertModeratorOwnsGame(Person moderator, VideoGame game) {
        if (moderator == null) return; //
        if (moderator.getStudio() == null
                || !moderator.getStudio().getId().equals(game.getStudio().getId())) {
            throw new ValidationException(
                    "You can only manage games that belong to your studio.");
        }
    }


    public List<VideoGame> getAllVideoGames() {
        return videoGameRepository.findAll();
    }

    public VideoGame getVideoGameById(UUID id) {
        return videoGameRepository.findById(id)
                .orElseThrow(() -> new ValidationException(
                        "Video Game with ID " + id + " not found."));
    }

    public VideoGame getVideoGameByTitle(String title) {
        return videoGameRepository.findByTitle(title)
                .orElseThrow(() -> new ValidationException(
                        "Video Game with title " + title + " not found."));
    }

    public VideoGame addVideoGame(VideoGameCreateDTO dto) {
        if (videoGameRepository.existsByTitle(dto.getTitle())) {
            throw new ValidationException("A video game with this title already exists.");
        }

        Studio studio = studioRepository.findById(dto.getStudioId())
                .orElseThrow(() -> new ValidationException(
                        "Studio with ID " + dto.getStudioId() + " not found."));

        Person moderator = getModeratorIfApplicable();
        assertModeratorOwnsGame(moderator,
                buildGameWithStudio(studio));

        VideoGame game = new VideoGame();
        game.setTitle(dto.getTitle());
        game.setPrice(dto.getPrice());
        game.setCoverImageUrl(dto.getCoverImageUrl());
        game.setStudio(studio);

        return videoGameRepository.save(game);
    }

    public VideoGame updateVideoGame(UUID gameId, VideoGameCreateDTO dto) {
        VideoGame existing = videoGameRepository.findById(gameId)
                .orElseThrow(() -> new ValidationException(
                        "Video Game with ID " + gameId + " not found."));

        Person moderator = getModeratorIfApplicable();
        assertModeratorOwnsGame(moderator, existing);

        recordPriceIfChanged(existing, dto.getPrice());

        existing.setTitle(dto.getTitle());
        existing.setPrice(dto.getPrice());
        existing.setCoverImageUrl(dto.getCoverImageUrl());

        if (dto.getStudioId() != null && moderator == null) {
            studioRepository.findById(dto.getStudioId())
                    .ifPresent(existing::setStudio);
        }

        return videoGameRepository.save(existing);
    }

    @Transactional
    public void deleteVideoGame(UUID gameId) {
        VideoGame game = videoGameRepository.findById(gameId)
                .orElseThrow(() -> new ValidationException(
                        "Cannot delete. Video Game with ID " + gameId + " not found."));

        Person moderator = getModeratorIfApplicable();
        assertModeratorOwnsGame(moderator, game);

        for (Person owner : game.getOwners()) {
            owner.getOwnedGames().remove(game);
            personRepository.save(owner);
        }

        videoGameRepository.deleteById(gameId);
    }

    public void patchVideoGame(UUID uuid, Map<String, Object> updates) {
        VideoGame existing = videoGameRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Video Game not found."));

        Person moderator = getModeratorIfApplicable();
        assertModeratorOwnsGame(moderator, existing);

        if (updates.containsKey("title")) {
            String newTitle = (String) updates.get("title");
            if (!existing.getTitle().equals(newTitle)
                    && videoGameRepository.existsByTitle(newTitle)) {
                throw new ValidationException("Title already exists.");
            }
            existing.setTitle(newTitle);
        }
        if (updates.containsKey("price")) {
            Double newPrice = ((Number) updates.get("price")).doubleValue();
            recordPriceIfChanged(existing, newPrice);
            existing.setPrice(newPrice);
        }
        if (updates.containsKey("coverImageUrl")) {
            existing.setCoverImageUrl((String) updates.get("coverImageUrl"));
        }
        if (updates.containsKey("studioId") && moderator == null) {
            UUID newStudioId = UUID.fromString((String) updates.get("studioId"));
            Studio s = studioRepository.findById(newStudioId)
                    .orElseThrow(() -> new ValidationException("Studio not found."));
            existing.setStudio(s);
        }

        videoGameRepository.save(existing);
    }

    public List<VideoGame> getFilteredVideoGames(String title, String studioName,
                                                 Double maxPrice, String sortBy,
                                                 String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        String processedTitle = (title == null || title.isBlank())
                ? null
                : "%" + title.toLowerCase() + "%";
        return videoGameRepository.searchAndFilterGames(
                processedTitle, studioName, maxPrice, sort);
    }


    private VideoGame buildGameWithStudio(Studio studio) {
        VideoGame tmp = new VideoGame();
        tmp.setStudio(studio);
        return tmp;
    }

    private void recordPriceIfChanged(VideoGame game, Double newPrice) {
        if (newPrice != null && !newPrice.equals(game.getPrice())) {
            PriceHistory point = new PriceHistory();
            point.setVideoGame(game);
            point.setPrice(newPrice);
            point.setRecordedAt(LocalDate.now());
            priceHistoryRepository.save(point);
        }
    }
}