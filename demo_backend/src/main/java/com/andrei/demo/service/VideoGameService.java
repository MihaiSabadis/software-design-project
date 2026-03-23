package com.andrei.demo.service;

import com.andrei.demo.model.VideoGame;
import com.andrei.demo.model.VideoGameCreateDTO;
import com.andrei.demo.repository.VideoGameRepository;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class VideoGameService {
    private final VideoGameRepository videoGameRepository;

    public List<VideoGame> getAllVideoGames() {
        return videoGameRepository.findAll();
    }

    public VideoGame getVideoGameById(UUID id) throws ValidationException {
        return videoGameRepository.findById(id)
                .orElseThrow(()->new ValidationException("Video Game with ID" + id + "not found."));
    }

    public VideoGame getVideoGameByTitle(String title) throws ValidationException {
        return videoGameRepository.findByTitle(title)
                .orElseThrow(()->new ValidationException("Video Game with title" + title + "not found."));
    }

    public VideoGame addVideoGame(VideoGameCreateDTO videoGameDTO){
        VideoGame videoGame = new VideoGame();

        videoGame.setTitle(videoGameDTO.getTitle());
        videoGame.setPrice(videoGameDTO.getPrice());
        return videoGameRepository.save(videoGame);
    }

    public VideoGame updateVideoGame(UUID id, VideoGame videoGame) throws ValidationException {
        Optional<VideoGame> videoGameOptional = videoGameRepository.findById(id);

        if(videoGameOptional.isEmpty()) {
            throw new ValidationException("Video Game with ID" + id + "not found.");
        }
        VideoGame existingVideoGame = videoGameOptional.get();

        existingVideoGame.setTitle(videoGame.getTitle());
        existingVideoGame.setPrice(videoGame.getPrice());

        return videoGameRepository.save(existingVideoGame);
    }

    public void deleteVideoGame(UUID id) throws ValidationException {
        if(videoGameRepository.existsById(id)) {
            throw new ValidationException("Cannot delete.Video Game with ID" + id + "not found.");
        }
        videoGameRepository.deleteById(id);
    }

}
