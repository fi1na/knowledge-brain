package com.knowledgebrain.repository;

import com.knowledgebrain.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface NoteRepository extends JpaRepository<Note, UUID> {

    Page<Note> findByUserId(UUID userId, Pageable pageable);

    Optional<Note> findByIdAndUserId(UUID id, UUID userId);

    void deleteByIdAndUserId(UUID id, UUID userId);

    long countByUserId(UUID userId);

    /**
     * Full-text search across title (weight A) and content (weight B).
     * Uses plainto_tsquery for safe input handling — no special syntax required from users.
     * ts_rank scores relevance; title matches rank higher due to weight A.
     * ts_headline generates a snippet with matching terms highlighted in bold tags.
     * Results are scoped to the requesting user and ordered by relevance descending.
     */
    @Query(
            value = """
            SELECT
                n.id,
                n.title,
                n.content,
                n.user_id AS userId,
                n.created_at AS createdAt,
                n.updated_at AS updatedAt,
                ts_rank(n.search_vector, plainto_tsquery('english', :query)) AS rank,
                ts_headline('english', coalesce(n.title, '') || ' ' || coalesce(n.content, ''),
                    plainto_tsquery('english', :query),
                    'StartSel=<b>, StopSel=</b>, MaxWords=35, MinWords=15, MaxFragments=2'
                ) AS headline
            FROM notes n
            WHERE n.user_id = :userId
              AND n.search_vector @@ plainto_tsquery('english', :query)
            ORDER BY rank DESC
            """,
            countQuery = """
            SELECT count(*)
            FROM notes n
            WHERE n.user_id = :userId
              AND n.search_vector @@ plainto_tsquery('english', :query)
            """,
            nativeQuery = true
    )
    Page<NoteSearchProjection> searchNotes(
            @Param("userId") UUID userId,
            @Param("query") String query,
            Pageable pageable
    );
}