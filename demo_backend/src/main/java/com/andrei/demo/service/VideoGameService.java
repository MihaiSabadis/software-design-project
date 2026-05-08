package com.andrei.demo.service;

import com.andrei.demo.model.Studio;
import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.dto.VideoGameCreateDTO;
import com.andrei.demo.repository.StudioRepository;
import com.andrei.demo.repository.VideoGameRepository;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VideoGameService {

    private final VideoGameRepository videoGameRepository;
    private final StudioRepository studioRepository; // <-- Adăugat pentru a putea lucra cu Studiouri

    public List<VideoGame> getAllVideoGames() {
        return videoGameRepository.findAll();
    }

    public VideoGame getVideoGameById(UUID id) throws ValidationException {
        return videoGameRepository.findById(id)
                .orElseThrow(()->new ValidationException("Video Game with ID " + id + " not found."));
    }

    public VideoGame getVideoGameByTitle(String title) throws ValidationException {
        return videoGameRepository.findByTitle(title)
                .orElseThrow(()->new ValidationException("Video Game with title " + title + " not found."));
    }

    public VideoGame addVideoGame(VideoGameCreateDTO videoGameDTO){

        if (videoGameRepository.existsByTitle(videoGameDTO.getTitle())) {
            throw new ValidationException("A video game with this title already exists!");
        }

        VideoGame videoGame = new VideoGame();
        videoGame.setTitle(videoGameDTO.getTitle());
        videoGame.setPrice(videoGameDTO.getPrice());
        videoGame.setCoverImageUrl(videoGameDTO.getCoverImageUrl()); // Asigură-te că pui și imaginea dacă o ai în DTO

        // --- LOGICA NOUĂ PENTRU STUDIO ---
        if (videoGameDTO.getStudioId() != null) {
            Studio studio = studioRepository.findById(videoGameDTO.getStudioId())
                    .orElseThrow(() -> new ValidationException("Studio with ID " + videoGameDTO.getStudioId() + " not found!"));
            videoGame.setStudio(studio);
        } else {
            throw new ValidationException("A video game must belong to a Studio!");
        }

        return videoGameRepository.save(videoGame);
    }

    public VideoGame updateVideoGame(UUID id, VideoGame videoGame) throws ValidationException {
        Optional<VideoGame> videoGameOptional = videoGameRepository.findById(id);

        if(videoGameOptional.isEmpty()) {
            throw new ValidationException("Video Game with ID " + id + " not found.");
        }
        VideoGame existingVideoGame = videoGameOptional.get();

        existingVideoGame.setTitle(videoGame.getTitle());
        existingVideoGame.setPrice(videoGame.getPrice());
        existingVideoGame.setCoverImageUrl(videoGame.getCoverImageUrl());

        if (videoGame.getStudio() != null) {
            existingVideoGame.setStudio(videoGame.getStudio());
        }

        return videoGameRepository.save(existingVideoGame);
    }

    public void patchVideoGame(UUID uuid, Map<String, Object> updates){
        VideoGame existingVideoGame = videoGameRepository.findById(uuid)
                .orElseThrow(()-> new ValidationException("Video Game not found."));

        if(updates.containsKey("title")){
            String newTitle = (String) updates.get("title");

            if(!existingVideoGame.getTitle().equals(newTitle) && videoGameRepository.existsByTitle(newTitle)){
                throw new ValidationException("Title already exists.");
            }
            existingVideoGame.setTitle(newTitle);
        }

        if(updates.containsKey("studioId")){
            String studioIdStr = (String) updates.get("studioId");
            if(studioIdStr != null && !studioIdStr.trim().isEmpty()){
                UUID newStudioId = UUID.fromString(studioIdStr);
                Studio newStudio = studioRepository.findById(newStudioId)
                        .orElseThrow(() -> new ValidationException("Studio not found."));
                existingVideoGame.setStudio(newStudio);
            } else {
                throw new ValidationException("Studio ID cannot be empty.");
            }
        }

        if (updates.containsKey("price")){
            existingVideoGame.setPrice(((Number) updates.get("price")).doubleValue());
        }

        if (updates.containsKey("coverImageUrl")){
            existingVideoGame.setCoverImageUrl((String) updates.get("coverImageUrl"));
        }

        videoGameRepository.save(existingVideoGame);
    }

    public void deleteVideoGame(UUID id) throws ValidationException {
        if(!videoGameRepository.existsById(id)) {
            throw new ValidationException("Cannot delete. Video Game with ID " + id + " not found.");
        }
        videoGameRepository.deleteById(id);
    }

    public List<VideoGame> getFilteredVideoGames(String title, String studioName, Double maxPrice, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        String processedTitle = (title == null || title.trim().isEmpty()) ? null : "%" + title.toLowerCase() + "%";

        return videoGameRepository.searchAndFilterGames(processedTitle, studioName, maxPrice, sort);
    }
}