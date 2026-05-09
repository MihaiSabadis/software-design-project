// demo_backend/src/main/java/com/andrei/demo/service/StudioService.java
package com.andrei.demo.service;

import com.andrei.demo.model.Studio;
import com.andrei.demo.repository.StudioRepository;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class StudioService {

    private final StudioRepository studioRepository;

    public List<Studio> getAllStudios() {
        return studioRepository.findAll();
    }

    public Studio createStudio(String name, String description) {
        if (studioRepository.findByName(name).isPresent()) {
            throw new ValidationException("A studio with this name already exists.");
        }
        Studio studio = new Studio();
        studio.setName(name);
        studio.setDescription(description);
        return studioRepository.save(studio);
    }

    public void deleteStudio(UUID id) {
        if (!studioRepository.existsById(id)) {
            throw new ValidationException("Studio with ID " + id + " not found.");
        }
        studioRepository.deleteById(id);
    }
}