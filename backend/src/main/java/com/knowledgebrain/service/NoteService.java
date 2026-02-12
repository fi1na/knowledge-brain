package com.knowledgebrain.service;

import com.knowledgebrain.dto.PagedResponse;
import com.knowledgebrain.dto.note.CreateNoteRequest;
import com.knowledgebrain.dto.note.NoteResponse;
import com.knowledgebrain.dto.note.UpdateNoteRequest;
import com.knowledgebrain.entity.Note;
import com.knowledgebrain.exception.ResourceNotFoundException;
import com.knowledgebrain.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Transactional
    public NoteResponse createNote(UUID userId, CreateNoteRequest request) {
        Note note = Note.builder()
                .userId(userId)
                .title(request.getTitle().trim())
                .content(request.getContent())
                .build();

        Note saved = noteRepository.save(note);
        log.debug("Created note {} for user {}", saved.getId(), userId);
        return NoteResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public NoteResponse getNoteById(UUID userId, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));
        return NoteResponse.from(note);
    }

    @Transactional(readOnly = true)
    public PagedResponse<NoteResponse> getNotes(UUID userId, int page, int size) {
        size = Math.min(size, 50);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        Page<Note> notePage = noteRepository.findByUserId(userId, pageable);
        return PagedResponse.from(notePage, NoteResponse::from);
    }

    @Transactional
    public NoteResponse updateNote(UUID userId, UUID noteId, UpdateNoteRequest request) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));

        if (request.getTitle() != null) {
            note.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }

        Note updated = noteRepository.save(note);
        log.debug("Updated note {} for user {}", noteId, userId);
        return NoteResponse.from(updated);
    }

    @Transactional
    public void deleteNote(UUID userId, UUID noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note", "id", noteId));
        noteRepository.delete(note);
        log.debug("Deleted note {} for user {}", noteId, userId);
    }
}
