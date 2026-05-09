package com.andrei.demo.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class StudioCreateDTO {
    @NotNull(message = "Name is required")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;
}
