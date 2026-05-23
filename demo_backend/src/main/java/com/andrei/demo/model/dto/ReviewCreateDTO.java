package com.andrei.demo.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCreateDTO {

    @NotNull(message = "Score is required")
    @Min(value = 1, message = "Score must be at least 1")
    @Max(value = 5, message = "Score cannot be greater than 5")
    private Integer score;

    @Size(max = 1000, message = "Comment cannot exceed 1000 characters")
    private String comment;

    @NotNull(message = "Author ID is required")
    private UUID authorId;

    @NotNull(message = "Game ID is required")
    private UUID gameId;
}