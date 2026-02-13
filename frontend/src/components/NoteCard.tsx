"use client";

import { useRouter } from "next/navigation";
import { Note } from "@/stores/noteStore";

interface NoteCardProps {
  note: Note;
  highlight?: string; // HTML headline from search
}

export default function NoteCard({ note, highlight }: NoteCardProps) {
  const router = useRouter();

  function formatDate(iso: string): string {
    return new Date(iso).toLocaleDateString("en-US", {
      month: "short",
      day: "numeric",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  }

  return (
    <div
      onClick={() => router.push(`/dashboard/notes/${note.id}`)}
      className="cursor-pointer rounded border border-gray-200 bg-white p-4 transition hover:border-gray-300 hover:shadow-sm"
    >
      <h3 className="font-medium text-gray-900">{note.title}</h3>

      {highlight ? (
        <p
          className="mt-1 line-clamp-2 text-sm text-gray-600"
          dangerouslySetInnerHTML={{ __html: highlight }}
        />
      ) : (
        <p className="mt-1 line-clamp-2 text-sm text-gray-600">
          {note.content || "No content"}
        </p>
      )}

      <p className="mt-2 text-xs text-gray-400">
        Updated {formatDate(note.updatedAt)}
      </p>
    </div>
  );
}
