package com.knowledgebrain.dto.websocket;

import com.knowledgebrain.dto.note.NoteResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        CREATED, UPDATED, DELETED
    }

    private Type type;
    private UUID noteId;
    private NoteResponse note;  // null for DELETED events
    private Instant timestamp;

    public static NoteEvent created(NoteResponse note) {
        return NoteEvent.builder()
                .type(Type.CREATED)
                .noteId(note.getId())
                .note(note)
                .timestamp(Instant.now())
                .build();
    }

    public static NoteEvent updated(NoteResponse note) {
        return NoteEvent.builder()
                .type(Type.UPDATED)
                .noteId(note.getId())
                .note(note)
                .timestamp(Instant.now())
                .build();
    }

    public static NoteEvent deleted(UUID noteId) {
        return NoteEvent.builder()
                .type(Type.DELETED)
                .noteId(noteId)
                .note(null)
                .timestamp(Instant.now())
                .build();
    }
}