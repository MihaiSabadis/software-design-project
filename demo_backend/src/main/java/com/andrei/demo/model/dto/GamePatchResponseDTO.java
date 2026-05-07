package com.andrei.demo.model.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class GamePatchResponseDTO {
    private UUID id;
    private String version;
    private String description;
    private LocalDate releaseDate;
    private UUID videoGameId;
}