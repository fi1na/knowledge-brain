import { Client, IMessage } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { getAccessToken } from "./axios";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8080/ws";

export interface NoteEvent {
  type: "CREATED" | "UPDATED" | "DELETED";
  noteId: string;
  note: NoteData | null;
  timestamp: string;
}

export interface NoteData {
  id: string;
  title: string;
  content: string | null;
  userId: string;
  createdAt: string;
  updatedAt: string;
}

let stompClient: Client | null = null;

export function connectWebSocket(
  onNoteEvent: (event: NoteEvent) => void
): void {
  if (stompClient?.active) return;

  const token = getAccessToken();
  if (!token) return;

  stompClient = new Client({
    webSocketFactory: () => new SockJS(WS_URL),
    connectHeaders: {
      Authorization: `Bearer ${token}`,
    },
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    onConnect: () => {
      console.log("[WS] Connected");
      stompClient!.subscribe("/user/queue/notes", (message: IMessage) => {
        try {
          const event: NoteEvent = JSON.parse(message.body);
          onNoteEvent(event);
        } catch (e) {
          console.error("[WS] Failed to parse message:", e);
        }
      });
    },
    onStompError: (frame) => {
      console.error("[WS] STOMP error:", frame.headers["message"]);
    },
    onDisconnect: () => {
      console.log("[WS] Disconnected");
    },
  });

  stompClient.activate();
}

export function disconnectWebSocket(): void {
  if (stompClient?.active) {
    stompClient.deactivate();
    stompClient = null;
    console.log("[WS] Deactivated");
  }
}
