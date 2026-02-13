"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, refresh } = useAuthStore();

  useEffect(() => {
    // Try to refresh session on app load
    refresh().then(() => {
      const auth = useAuthStore.getState().isAuthenticated;
      router.replace(auth ? "/dashboard" : "/login");
    });
  }, [refresh, router]);

  return (
    <div className="flex h-screen items-center justify-center">
      <p className="text-gray-500">Loading...</p>
    </div>
  );
}
