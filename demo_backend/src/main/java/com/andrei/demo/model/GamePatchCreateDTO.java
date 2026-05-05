package com.andrei.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class GamePatchCreateDTO {

    @NotBlank(message = "Version of patch is required")
    @Size(max = 20, message = "Too long version name")
    private String version;

    @Size(max = 1000, message = "Description can't exceed 1000 characters")
    private String description;

    @NotNull(message = "Release Date is required")
    @PastOrPresent(message = "Release Date can't be set in the future")
    private LocalDate releaseDate;
}