"use client";

import { useEffect, useState, useCallback, useRef } from "react";
import { useRouter, useParams } from "next/navigation";
import { useNoteStore } from "@/stores/noteStore";

export default function NoteEditorPage() {
  const router = useRouter();
  const params = useParams();
  const noteId = params.id as string;

  const {
    currentNote,
    isLoading,
    error,
    fetchNote,
    updateNote,
    deleteNote,
    clearCurrentNote,
  } = useNoteStore();

  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [saving, setSaving] = useState(false);
  const [lastSaved, setLastSaved] = useState<string | null>(null);
  const [showDelete, setShowDelete] = useState(false);

  // Track if user has edited to avoid overwriting with stale data
  const initializedRef = useRef(false);
  const saveTimeoutRef = useRef<NodeJS.Timeout | null>(null);

  // Fetch note on mount
  useEffect(() => {
    fetchNote(noteId);
    return () => {
      clearCurrentNote();
    };
  }, [noteId, fetchNote, clearCurrentNote]);

  // Populate fields when note loads
  useEffect(() => {
    if (currentNote && !initializedRef.current) {
      setTitle(currentNote.title);
      setContent(currentNote.content || "");
      initializedRef.current = true;
    }
  }, [currentNote]);

  // Auto-save with debounce (1.5s after last keystroke)
  const debouncedSave = useCallback(
    (newTitle: string, newContent: string) => {
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current);
      }
      saveTimeoutRef.current = setTimeout(async () => {
        setSaving(true);
        try {
          await updateNote(noteId, newTitle, newContent);
          setLastSaved(new Date().toLocaleTimeString());
        } catch {
          // Error in store
        } finally {
          setSaving(false);
        }
      }, 1500);
    },
    [noteId, updateNote]
  );

  // Clean up timeout on unmount
  useEffect(() => {
    return () => {
      if (saveTimeoutRef.current) {
        clearTimeout(saveTimeoutRef.current);
      }
    };
  }, []);

  function handleTitleChange(value: string) {
    setTitle(value);
    debouncedSave(value, content);
  }

  function handleContentChange(value: string) {
    setContent(value);
    debouncedSave(title, value);
  }

  async function handleDelete() {
    await deleteNote(noteId);
    router.push("/dashboard");
  }

  if (isLoading && !currentNote) {
    return (
      <div className="flex h-full items-center justify-center">
        <p className="text-gray-500">Loading note...</p>
      </div>
    );
  }

  if (error && !currentNote) {
    return (
      <div className="flex h-full flex-col items-center justify-center gap-4">
        <p className="text-red-600">{error}</p>
        <button
          onClick={() => router.push("/dashboard")}
          className="text-sm text-blue-600 hover:underline"
        >
          Back to notes
        </button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-3xl">
      {/* Toolbar */}
      <div className="mb-4 flex items-center justify-between">
        <button
          onClick={() => router.push("/dashboard")}
          className="text-sm text-gray-500 hover:text-gray-700"
        >
          &larr; Back
        </button>

        <div className="flex items-center gap-3">
          {saving && (
            <span className="text-xs text-gray-400">Saving...</span>
          )}
          {!saving && lastSaved && (
            <span className="text-xs text-gray-400">
              Saved at {lastSaved}
            </span>
          )}

          <button
            onClick={() => setShowDelete(true)}
            className="rounded border border-red-200 px-3 py-1.5 text-xs text-red-600 hover:bg-red-50"
          >
            Delete
          </button>
        </div>
      </div>

      {/* Title */}
      <input
        type="text"
        value={title}
        onChange={(e) => handleTitleChange(e.target.value)}
        placeholder="Note title"
        className="mb-4 w-full border-0 bg-transparent text-2xl font-bold text-gray-900 placeholder-gray-400 focus:outline-none"
      />

      {/* Content */}
      <textarea
        value={content}
        onChange={(e) => handleContentChange(e.target.value)}
        placeholder="Start writing..."
        className="min-h-[60vh] w-full resize-none border-0 bg-transparent text-base leading-relaxed text-gray-700 placeholder-gray-400 focus:outline-none"
      />

      {/* Delete confirmation */}
      {showDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="w-full max-w-xs rounded bg-white p-6 shadow-lg">
            <p className="mb-4 text-sm text-gray-700">
              Delete this note? This cannot be undone.
            </p>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowDelete(false)}
                className="rounded px-3 py-1.5 text-sm text-gray-600 hover:bg-gray-100"
              >
                Cancel
              </button>
              <button
                onClick={handleDelete}
                className="rounded bg-red-600 px-3 py-1.5 text-sm text-white hover:bg-red-700"
              >
                Delete
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
