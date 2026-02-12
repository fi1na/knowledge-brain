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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private static final int MAX_PAGE_SIZE = 50;

    private final NoteRepository noteRepository;

    @Transactional
    public NoteResponse createNote(UUID userId, CreateNoteRequest request) {
        Note note = Note.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .content(request.getContent())
                .build();

        Note saved = noteRepository.save(note);
        log.info("Note created: id={}, userId={}, title=\"{}\"", saved.getId(), userId, saved.getTitle());
        return NoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NoteResponse> getNotes(UUID userId, int page, int size) {
        int cappedSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, cappedSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Note> notePage = noteRepository.findByUserId(userId, pageable);
        log.debug("Notes listed: userId={}, page={}, size={}, totalElements={}", userId, page, cappedSize, notePage.getTotalElements());
        return new PagedResponse<>(notePage, NoteResponse::from);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(UUID userId, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    log.warn("Note not found: noteId={}, userId={}", noteId, userId);
                    return new ResourceNotFoundException("Note", "id", noteId);
                });
        log.debug("Note fetched: noteId={}, userId={}", noteId, userId);
        return NoteResponse.from(note);
    }

    @Transactional
    public NoteResponse updateNote(UUID userId, UUID noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    log.warn("Note not found for update: noteId={}, userId={}", noteId, userId);
                    return new ResourceNotFoundException("Note", "id", noteId);
                });

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        Note updated = noteRepository.save(note);
        log.info("Note updated: noteId={}, userId={}", noteId, userId);
        return NoteResponse.from(updated);
    }

    @Transactional
    public void deleteNote(UUID userId, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    log.warn("Note not found for delete: noteId={}, userId={}", noteId, userId);
                    return new ResourceNotFoundException("Note", "id", noteId);
                });
        noteRepository.delete(note);
        log.info("Note deleted: noteId={}, userId={}", noteId, userId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NoteSearchResponse> searchNotes(UUID userId, String query, int page, int size) {
        int cappedSize = Math.min(size, MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, cappedSize);

        Page<NoteSearchProjection> results = noteRepository.searchNotes(userId, query.trim(), pageable);
        log.info("Search: userId={}, query=\"{}\", results={}", userId, query.trim(), results.getTotalElements());

        return new PagedResponse<>(results, this::toSearchResponse);
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