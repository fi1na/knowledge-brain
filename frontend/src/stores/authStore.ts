import { create } from "zustand";
import api, { setAccessToken } from "@/lib/axios";

interface User {
  userId: string;
  email: string;
  displayName: string;
}

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  register: (email: string, password: string, displayName: string) => Promise<void>;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  refresh: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: false,
  isLoading: false,
  error: null,

  register: async (email, password, displayName) => {
    set({ isLoading: true, error: null });
    try {
      const { data } = await api.post("/api/auth/register", {
        email,
        password,
        displayName,
      });
      setAccessToken(data.accessToken);
      set({
        user: {
          userId: data.userId,
          email: data.email,
          displayName: data.displayName,
        },
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (err: unknown) {
      const message = extractError(err);
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  login: async (email, password) => {
    set({ isLoading: true, error: null });
    try {
      const { data } = await api.post("/api/auth/login", { email, password });
      setAccessToken(data.accessToken);
      set({
        user: {
          userId: data.userId,
          email: data.email,
          displayName: data.displayName,
        },
        isAuthenticated: true,
        isLoading: false,
      });
    } catch (err: unknown) {
      const message = extractError(err);
      set({ error: message, isLoading: false });
      throw err;
    }
  },

  logout: async () => {
    try {
      await api.post("/api/auth/logout");
    } catch {
      // Ignore - clear local state regardless
    } finally {
      setAccessToken(null);
      set({ user: null, isAuthenticated: false });
    }
  },

  refresh: async () => {
    try {
      const { data } = await api.post("/api/auth/refresh");
      setAccessToken(data.accessToken);
      set({
        user: {
          userId: data.userId,
          email: data.email,
          displayName: data.displayName,
        },
        isAuthenticated: true,
      });
    } catch {
      setAccessToken(null);
      set({ user: null, isAuthenticated: false });
    }
  },

  clearError: () => set({ error: null }),
}));

function extractError(err: unknown): string {
  if (
    typeof err === "object" &&
    err !== null &&
    "response" in err
  ) {
    const resp = (err as { response?: { data?: { message?: string; error?: string } } }).response;
    return resp?.data?.message || resp?.data?.error || "Something went wrong";
  }
  return "Network error";
}
