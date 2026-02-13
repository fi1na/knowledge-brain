"use client";

import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";
import { disconnectWebSocket } from "@/lib/websocket";

export default function Sidebar() {
  const router = useRouter();
  const { user, logout } = useAuthStore();

  async function handleLogout() {
    disconnectWebSocket();
    await logout();
    router.push("/login");
  }

  return (
    <aside className="flex h-screen w-56 flex-col border-r border-gray-200 bg-white">
      {/* Header */}
      <div className="border-b border-gray-200 px-4 py-4">
        <h1 className="text-lg font-bold text-gray-800">Knowledge Brain</h1>
      </div>

      {/* Nav */}
      <nav className="flex-1 px-3 py-4">
        <button
          onClick={() => router.push("/dashboard")}
          className="w-full rounded px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
        >
          All Notes
        </button>
      </nav>

      {/* User section */}
      <div className="border-t border-gray-200 px-4 py-3">
        <p className="truncate text-sm font-medium text-gray-800">
          {user?.displayName}
        </p>
        <p className="truncate text-xs text-gray-500">{user?.email}</p>
        <button
          onClick={handleLogout}
          className="mt-2 w-full rounded border border-gray-300 px-3 py-1.5 text-xs text-gray-600 hover:bg-gray-50"
        >
          Sign out
        </button>
      </div>
    </aside>
  );
}
