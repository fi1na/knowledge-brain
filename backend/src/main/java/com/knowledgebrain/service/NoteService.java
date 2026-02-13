package com.knowledgebrain.service;

import com.knowledgebrain.dto.PagedResponse;
import com.knowledgebrain.dto.note.CreateNoteRequest;
import com.knowledgebrain.dto.note.NoteResponse;
import com.knowledgebrain.dto.note.NoteSearchResponse;
import com.knowledgebrain.dto.note.UpdateNoteRequest;
import com.knowledgebrain.entity.Note;
import com.knowledgebrain.exception.ResourceNotFoundException;
import com.knowledgebrain.repository.NoteRepository;
import com.knowledgebrain.repository.NoteSearchProjection;
import com.knowledgebrain.websocket.NoteEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteEventPublisher noteEventPublisher;

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notes", allEntries = true),
            @CacheEvict(value = "noteSearch", allEntries = true)
    })
    public NoteResponse createNote(UUID userId, CreateNoteRequest request) {
        Note note = Note.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .content(request.getContent())
                .build();

        Note saved = noteRepository.save(note);
        log.info("Created note {} for user {}", saved.getId(), userId);

        NoteResponse response = NoteResponse.from(saved);
        noteEventPublisher.publishCreated(userId, response);
        return response;
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(UUID userId, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    log.warn("Note not found: {} for user {}", noteId, userId);
                    return new ResourceNotFoundException("Note", "id", noteId);
                });
        log.debug("Fetched note {} for user {}", noteId, userId);
        return NoteResponse.from(note);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "notes", key = "#userId + ':' + #page + ':' + #size")
    public PagedResponse<NoteResponse> getNotes(UUID userId, int page, int size) {
        size = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Note> notePage = noteRepository.findByUserId(userId, pageable);
        log.debug("Listed notes for user {} (page={}, size={})", userId, page, size);
        return PagedResponse.from(notePage, NoteResponse::from);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notes", allEntries = true),
            @CacheEvict(value = "noteSearch", allEntries = true)
    })
    public NoteResponse updateNote(UUID userId, UUID noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    log.warn("Note not found for update: {} for user {}", noteId, userId);
                    return new ResourceNotFoundException("Note", "id", noteId);
                });

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        Note updated = noteRepository.save(note);
        log.info("Updated note {} for user {}", noteId, userId);

        NoteResponse response = NoteResponse.from(updated);
        noteEventPublisher.publishUpdated(userId, response);
        return response;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "notes", allEntries = true),
            @CacheEvict(value = "noteSearch", allEntries = true)
    })
    public void deleteNote(UUID userId, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    log.warn("Note not found for delete: {} for user {}", noteId, userId);
                    return new ResourceNotFoundException("Note", "id", noteId);
                });
        noteRepository.delete(note);
        log.info("Deleted note {} for user {}", noteId, userId);

        noteEventPublisher.publishDeleted(userId, noteId);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "noteSearch", key = "#userId + ':' + #query + ':' + #page + ':' + #size")
    public PagedResponse<NoteSearchResponse> searchNotes(UUID userId, String query, int page, int size) {
        size = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, size);
        Page<NoteSearchProjection> results = noteRepository.searchNotes(userId, query.trim(), pageable);
        log.info("Search '{}' for user {} returned {} results", query.trim(), userId, results.getTotalElements());
        return PagedResponse.from(results, this::toSearchResponse);
    }

    private NoteSearchResponse toSearchResponse(NoteSearchProjection projection) {
        return NoteSearchResponse.builder()
                .id(projection.getId())
                .title(projection.getTitle())
                .content(projection.getContent())
                .userId(projection.getUserId())
                .createdAt(projection.getCreatedAt())
                .updatedAt(projection.getUpdatedAt())
                .rank(projection.getRank())
                .headline(projection.getHeadline())
                .build();
    }
}