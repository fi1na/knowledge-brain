"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuthStore } from "@/stores/authStore";

export default function AuthGuard({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const { isAuthenticated, refresh } = useAuthStore();
  const [checked, setChecked] = useState(false);

  useEffect(() => {
    if (isAuthenticated) {
      setChecked(true);
      return;
    }
    // Try refresh before redirecting
    refresh().then(() => {
      const auth = useAuthStore.getState().isAuthenticated;
      if (!auth) {
        router.replace("/login");
      } else {
        setChecked(true);
      }
    });
  }, [isAuthenticated, refresh, router]);

  if (!checked) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-gray-500">Loading...</p>
      </div>
    );
  }

  return <>{children}</>;
}
