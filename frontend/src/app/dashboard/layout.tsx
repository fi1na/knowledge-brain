"use client";

import { useEffect } from "react";
import AuthGuard from "@/components/AuthGuard";
import Sidebar from "@/components/Sidebar";
import { connectWebSocket, disconnectWebSocket } from "@/lib/websocket";
import { useNoteStore } from "@/stores/noteStore";
import { useAuthStore } from "@/stores/authStore";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const handleNoteEvent = useNoteStore((s) => s.handleNoteEvent);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);

  useEffect(() => {
    if (isAuthenticated) {
      connectWebSocket(handleNoteEvent);
    }
    return () => {
      disconnectWebSocket();
    };
  }, [isAuthenticated, handleNoteEvent]);

  return (
    <AuthGuard>
      <div className="flex h-screen overflow-hidden">
        <Sidebar />
        <main className="flex-1 overflow-y-auto bg-gray-50 p-6">
          {children}
        </main>
      </div>
    </AuthGuard>
  );
}
