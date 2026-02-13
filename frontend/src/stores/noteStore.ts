import { create } from "zustand";
import api from "@/lib/axios";
import { NoteEvent } from "@/lib/websocket";

export interface Note {
  id: string;
  title: string;
  content: string | null;
  userId: string;
  createdAt: string;
  updatedAt: string;
}

export interface NoteSearchResult extends Note {
  rank: number;
  headline: string;
}

interface PageInfo {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

interface NoteState {
  notes: Note[];
  pageInfo: PageInfo;
  searchResults: NoteSearchResult[];
  searchPageInfo: PageInfo;
  currentNote: Note | null;
  isLoading: boolean;
  error: string | null;

  fetchNotes: (page?: number, size?: number) => Promise<void>;
  fetchNote: (id: string) => Promise<void>;
  createNote: (title: string, content?: string) => Promise<Note>;
  updateNote: (id: string, title?: string, content?: string) => Promise<Note>;
  deleteNote: (id: string) => Promise<void>;
  searchNotes: (query: string, page?: number, size?: number) => Promise<void>;
  handleNoteEvent: (event: NoteEvent) => void;
  clearCurrentNote: () => void;
  clearError: () => void;
}

const emptyPage: PageInfo = {
  page: 0,
  size: 20,
  totalElements: 0,
  totalPages: 0,
  first: true,
  last: true,
};

export const useNoteStore = create<NoteState>((set, get) => ({
  notes: [],
  pageInfo: { ...emptyPage },
  searchResults: [],
  searchPageInfo: { ...emptyPage },
  currentNote: null,
  isLoading: false,
  error: null,

  fetchNotes: async (page = 0, size = 20) => {
    set({ isLoading: true, error: null });
    try {
      const { data } = await api.get("/api/notes", {
        params: { page, size },
      });
      set({
        notes: data.content,
        pageInfo: {
          page: data.page,
          size: data.size,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          first: data.first,
          last: data.last,
        },
        isLoading: false,
      });
    } catch (err) {
      set({ error: extractError(err), isLoading: false });
    }
  },

  fetchNote: async (id) => {
    set({ isLoading: true, error: null });
    try {
      const { data } = await api.get(`/api/notes/${id}`);
      set({ currentNote: data, isLoading: false });
    } catch (err) {
      set({ error: extractError(err), isLoading: false });
    }
  },

  createNote: async (title, content) => {
    set({ isLoading: true, error: null });
    try {
      const { data } = await api.post("/api/notes", { title, content });
      // Prepend to list
      set((state) => ({
        notes: [data, ...state.notes],
        pageInfo: {
          ...state.pageInfo,
          totalElements: state.pageInfo.totalElements + 1,
        },
        isLoading: false,
      }));
      return data;
    } catch (err) {
      set({ error: extractError(err), isLoading: false });
      throw err;
    }
  },

  updateNote: async (id, title, content) => {
    set({ isLoading: true, error: null });
    try {
      const body: Record<string, string | undefined> = {};
      if (title !== undefined) body.title = title;
      if (content !== undefined) body.content = content;
      const { data } = await api.put(`/api/notes/${id}`, body);
      // Update in list
      set((state) => ({
        notes: state.notes.map((n) => (n.id === id ? data : n)),
        currentNote: state.currentNote?.id === id ? data : state.currentNote,
        isLoading: false,
      }));
      return data;
    } catch (err) {
      set({ error: extractError(err), isLoading: false });
      throw err;
    }
  },

  deleteNote: async (id) => {
    set({ isLoading: true, error: null });
    try {
      await api.delete(`/api/notes/${id}`);
      set((state) => ({
        notes: state.notes.filter((n) => n.id !== id),
        pageInfo: {
          ...state.pageInfo,
          totalElements: Math.max(0, state.pageInfo.totalElements - 1),
        },
        currentNote: state.currentNote?.id === id ? null : state.currentNote,
        isLoading: false,
      }));
    } catch (err) {
      set({ error: extractError(err), isLoading: false });
    }
  },

  searchNotes: async (query, page = 0, size = 20) => {
    set({ isLoading: true, error: null });
    try {
      const { data } = await api.get("/api/notes/search", {
        params: { q: query, page, size },
      });
      set({
        searchResults: data.content,
        searchPageInfo: {
          page: data.page,
          size: data.size,
          totalElements: data.totalElements,
          totalPages: data.totalPages,
          first: data.first,
          last: data.last,
        },
        isLoading: false,
      });
    } catch (err) {
      set({ error: extractError(err), isLoading: false });
    }
  },

  handleNoteEvent: (event: NoteEvent) => {
    const state = get();
    switch (event.type) {
      case "CREATED":
        if (event.note) {
          // Avoid duplicates
          const exists = state.notes.some((n) => n.id === event.noteId);
          if (!exists) {
            set({ notes: [event.note, ...state.notes] });
          }
        }
        break;
      case "UPDATED":
        if (event.note) {
          set({
            notes: state.notes.map((n) =>
              n.id === event.noteId ? event.note! : n
            ),
            currentNote:
              state.currentNote?.id === event.noteId
                ? event.note
                : state.currentNote,
          });
        }
        break;
      case "DELETED":
        set({
          notes: state.notes.filter((n) => n.id !== event.noteId),
          currentNote:
            state.currentNote?.id === event.noteId
              ? null
              : state.currentNote,
        });
        break;
    }
  },

  clearCurrentNote: () => set({ currentNote: null }),
  clearError: () => set({ error: null }),
}));

function extractError(err: unknown): string {
  if (typeof err === "object" && err !== null && "response" in err) {
    const resp = (err as { response?: { data?: { message?: string; error?: string } } }).response;
    return resp?.data?.message || resp?.data?.error || "Something went wrong";
  }
  return "Network error";
}
