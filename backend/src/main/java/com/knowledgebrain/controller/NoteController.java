package com.knowledgebrain.controller;

import com.knowledgebrain.dto.PagedResponse;
import com.knowledgebrain.dto.note.CreateNoteRequest;
import com.knowledgebrain.dto.note.NoteResponse;
import com.knowledgebrain.dto.note.NoteSearchResponse;
import com.knowledgebrain.dto.note.UpdateNoteRequest;
import com.knowledgebrain.security.UserPrincipal;
import com.knowledgebrain.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateNoteRequest request) {
        return noteService.createNote(principal.getId(), request);
    }

    @GetMapping
    public PagedResponse<NoteResponse> getNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return noteService.getNotes(principal.getId(), page, size);
    }

    /**
     * Full-text search. Must be mapped BEFORE /{noteId} to avoid
     * Spring interpreting "search" as a UUID path variable.
     */
    @GetMapping("/search")
    public PagedResponse<NoteSearchResponse> searchNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        if (query == null || query.isBlank()) {
            return PagedResponse.empty();
        }
        return noteService.searchNotes(principal.getId(), query, page, size);
    }

    @GetMapping("/{noteId}")
    public NoteResponse getNoteById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId) {
        return noteService.getNoteById(principal.getId(), noteId);
    }

    @PutMapping("/{noteId}")
    public NoteResponse updateNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        return noteService.updateNote(principal.getId(), noteId, request);
    }

    @DeleteMapping("/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId) {
        noteService.deleteNote(principal.getId(), noteId);
    }
}