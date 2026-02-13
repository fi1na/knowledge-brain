"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter } from "next/navigation";
import { useNoteStore } from "@/stores/noteStore";
import NoteCard from "@/components/NoteCard";
import SearchBar from "@/components/SearchBar";

export default function DashboardPage() {
  const router = useRouter();
  const {
    notes,
    pageInfo,
    searchResults,
    searchPageInfo,
    isLoading,
    fetchNotes,
    searchNotes,
    createNote,
  } = useNoteStore();

  const [isSearching, setIsSearching] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");

  useEffect(() => {
    fetchNotes();
  }, [fetchNotes]);

  const handleSearch = useCallback(
    (query: string) => {
      setIsSearching(true);
      setSearchQuery(query);
      searchNotes(query);
    },
    [searchNotes]
  );

  const handleClearSearch = useCallback(() => {
    setIsSearching(false);
    setSearchQuery("");
    fetchNotes();
  }, [fetchNotes]);

  async function handleCreateNote() {
    try {
      const note = await createNote("Untitled");
      router.push(`/dashboard/notes/${note.id}`);
    } catch {
      // Error in store
    }
  }

  const displayNotes = isSearching ? searchResults : notes;
  const displayPageInfo = isSearching ? searchPageInfo : pageInfo;

  function handlePageChange(newPage: number) {
    if (isSearching) {
      searchNotes(searchQuery, newPage);
    } else {
      fetchNotes(newPage);
    }
  }

  return (
    <div className="mx-auto max-w-3xl">
      {/* Header */}
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-xl font-bold text-gray-800">
          {isSearching ? `Search: "${searchQuery}"` : "All Notes"}
        </h1>
        <button
          onClick={handleCreateNote}
          className="rounded bg-blue-600 px-4 py-2 text-sm font-medium text-white hover:bg-blue-700"
        >
          + New Note
        </button>
      </div>

      {/* Search */}
      <div className="mb-4">
        <SearchBar onSearch={handleSearch} onClear={handleClearSearch} />
      </div>

      {/* Notes list */}
      {isLoading && displayNotes.length === 0 ? (
        <p className="py-8 text-center text-gray-500">Loading notes...</p>
      ) : displayNotes.length === 0 ? (
        <p className="py-8 text-center text-gray-500">
          {isSearching ? "No results found." : "No notes yet. Create your first note!"}
        </p>
      ) : (
        <div className="space-y-3">
          {displayNotes.map((note) => (
            <NoteCard
              key={note.id}
              note={note}
              highlight={
                isSearching && "headline" in note
                  ? (note as { headline?: string }).headline
                  : undefined
              }
            />
          ))}
        </div>
      )}

      {/* Pagination */}
      {displayPageInfo.totalPages > 1 && (
        <div className="mt-6 flex items-center justify-center gap-4">
          <button
            onClick={() => handlePageChange(displayPageInfo.page - 1)}
            disabled={displayPageInfo.first}
            className="rounded border border-gray-300 px-3 py-1.5 text-sm disabled:opacity-40"
          >
            Previous
          </button>
          <span className="text-sm text-gray-600">
            Page {displayPageInfo.page + 1} of {displayPageInfo.totalPages}
          </span>
          <button
            onClick={() => handlePageChange(displayPageInfo.page + 1)}
            disabled={displayPageInfo.last}
            className="rounded border border-gray-300 px-3 py-1.5 text-sm disabled:opacity-40"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
