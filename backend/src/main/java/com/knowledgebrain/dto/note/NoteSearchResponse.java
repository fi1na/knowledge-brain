package com.knowledgebrain.dto.note;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoteSearchResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID id;
    private String title;
    private String content;
    private UUID userId;
    private Instant createdAt;
    private Instant updatedAt;
    private double rank;
    private String headline;
}