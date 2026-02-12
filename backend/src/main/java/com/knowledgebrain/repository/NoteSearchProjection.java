package com.knowledgebrain.repository;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface projection for full-text search results.
 * Spring Data maps native query columns to these getters by name.
 */
public interface NoteSearchProjection {

    UUID getId();

    String getTitle();

    String getContent();

    UUID getUserId();

    Instant getCreatedAt();

    Instant getUpdatedAt();

    double getRank();

    String getHeadline();
}