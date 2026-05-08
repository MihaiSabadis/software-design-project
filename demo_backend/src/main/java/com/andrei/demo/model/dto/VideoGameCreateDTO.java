package com.andrei.demo.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class VideoGameCreateDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @Size(max = 1000, message = "URL of the image too long")
    private String coverImageUrl;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
}