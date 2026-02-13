package com.knowledgebrain.websocket;

import com.knowledgebrain.dto.note.NoteResponse;
import com.knowledgebrain.dto.websocket.NoteEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoteEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    private static final String DESTINATION = "/queue/notes";

    public void publishCreated(UUID userId, NoteResponse note) {
        NoteEvent event = NoteEvent.created(note);
        send(userId, event);
        log.info("Published NOTE_CREATED event for note {} to user {}", note.getId(), userId);
    }

    public void publishUpdated(UUID userId, NoteResponse note) {
        NoteEvent event = NoteEvent.updated(note);
        send(userId, event);
        log.info("Published NOTE_UPDATED event for note {} to user {}", note.getId(), userId);
    }

    public void publishDeleted(UUID userId, UUID noteId) {
        NoteEvent event = NoteEvent.deleted(noteId);
        send(userId, event);
        log.info("Published NOTE_DELETED event for note {} to user {}", noteId, userId);
    }

    private void send(UUID userId, NoteEvent event) {
        try {
            messagingTemplate.convertAndSendToUser(
                    userId.toString(),
                    DESTINATION,
                    event
            );
        } catch (Exception e) {
            // WebSocket delivery is best-effort. Never fail the HTTP request because of WS.
            log.error("Failed to publish WebSocket event: {}", e.getMessage(), e);
        }
    }
}