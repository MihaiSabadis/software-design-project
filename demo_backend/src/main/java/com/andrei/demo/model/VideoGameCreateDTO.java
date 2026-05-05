package com.andrei.demo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VideoGameCreateDTO {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Developer is required")
    private String developer;

    @Size(max = 1000, message = "URL-ul imaginii este prea lung")
    private String coverImageUrl;

    @NotNull(message = "Price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private Double price;
}