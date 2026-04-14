package com.noteapp.noteapp.service;

import com.noteapp.noteapp.model.Note;
import com.noteapp.noteapp.model.NoteVersion;
import com.noteapp.noteapp.repository.NoteRepository;
import com.noteapp.noteapp.repository.NoteVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final NoteVersionRepository noteVersionRepository;

    @Transactional
    public Note createNote(Note note) {
        Note savedNote = noteRepository.save(note);

        // Create initial version 1
        saveVersionEntry(savedNote, 1);

        return savedNote;
    }

    @Transactional
    public Note updateNote(Long id, Note noteDetails) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        Note updatedNote = noteRepository.save(note);

        // Calculate version number: current count of versions + 1
        int nextVersion = noteVersionRepository.findByNoteId(id).size() + 1;
        saveVersionEntry(updatedNote, nextVersion);

        return updatedNote;
    }

    public List<Note> getAllNotes() {
        return noteRepository.findAll();
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
    }

    public List<NoteVersion> getNoteHistory(Long noteId) {
        // Ensure note exists before fetching history
        if (!noteRepository.existsById(noteId)) {
            throw new RuntimeException("Note not found with id: " + noteId);
        }
        return noteVersionRepository.findByNoteId(noteId);
    }

    private void saveVersionEntry(Note note, int versionNumber) {
        NoteVersion version = NoteVersion.builder()
                .noteId(note.getId())
                .content(note.getContent())
                .versionNumber(versionNumber)
                .build();
        noteVersionRepository.save(version);
    }
}
