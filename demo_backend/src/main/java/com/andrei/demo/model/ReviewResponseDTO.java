package com.andrei.demo.model;

import lombok.Data;
import java.util.UUID;

@Data
public class ReviewResponseDTO {
    private UUID id;
    private Integer score;
    private String comment;

    private UUID authorId;
    private String authorName;
    private UUID gameId;
}