package com.knowledgebrain.controller;

import com.knowledgebrain.dto.PagedResponse;
import com.knowledgebrain.dto.note.CreateNoteRequest;
import com.knowledgebrain.dto.note.NoteResponse;
import com.knowledgebrain.dto.note.UpdateNoteRequest;
import com.knowledgebrain.security.UserPrincipal;
import com.knowledgebrain.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateNoteRequest request) {
        NoteResponse response = noteService.createNote(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{noteId}")
    public ResponseEntity<NoteResponse> getNoteById(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId) {
        NoteResponse response = noteService.getNoteById(principal.getId(), noteId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PagedResponse<NoteResponse>> getNotes(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<NoteResponse> response = noteService.getNotes(principal.getId(), page, size);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<NoteResponse> updateNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId,
            @Valid @RequestBody UpdateNoteRequest request) {
        NoteResponse response = noteService.updateNote(principal.getId(), noteId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> deleteNote(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID noteId) {
        noteService.deleteNote(principal.getId(), noteId);
        return ResponseEntity.noContent().build();
    }
}