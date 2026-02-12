package com.knowledgebrain.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteSearchResponse {

    private UUID id;
    private String title;
    private String content;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
    private double rank;
    private String headline;
}