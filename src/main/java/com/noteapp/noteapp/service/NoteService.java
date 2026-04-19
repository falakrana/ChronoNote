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
        if (note.getDeleted() == null) {
            note.setDeleted(false);
        }
        Note savedNote = noteRepository.save(note);

        // Create initial version 1
        saveVersionEntry(savedNote, 1);

        return savedNote;
    }

    @Transactional
    public Note updateNote(Long id, Note noteDetails) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));

        // Check if anything actually changed
        boolean isChanged = !note.getTitle().equals(noteDetails.getTitle()) || 
                           !note.getContent().equals(noteDetails.getContent());

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        Note updatedNote = noteRepository.save(note);

        // Only create a new version if there were actual changes
        if (isChanged) {
            int nextVersion = noteVersionRepository.findByNoteId(id).size() + 1;
            saveVersionEntry(updatedNote, nextVersion);
        }

        return updatedNote;
    }


    public List<Note> getAllNotes() {
        return noteRepository.findByDeletedFalse();
    }

    public List<Note> getTrashNotes() {
        return noteRepository.findByDeletedTrue();
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .filter(note -> note.getDeleted() == null || !note.getDeleted())
                .orElseThrow(() -> new RuntimeException("Note not found or is in trash with id: " + id));
    }

    @Transactional
    public void softDelete(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
        note.setDeleted(true);
        noteRepository.save(note);
    }

    @Transactional
    public void restoreNote(Long id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
        note.setDeleted(false);
        noteRepository.save(note);
    }

    @Transactional
    public void hardDelete(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new RuntimeException("Note not found with id: " + id);
        }
        // Delete history first (Cascade)
        noteVersionRepository.deleteByNoteId(id);
        // Delete note
        noteRepository.deleteById(id);
    }

    public List<NoteVersion> getNoteHistory(Long noteId) {
        // History should be accessible even if note is soft-deleted for auditing
        if (!noteRepository.existsById(noteId)) {
            throw new RuntimeException("Note not found with id: " + noteId);
        }
        return noteVersionRepository.findByNoteId(noteId);
    }

    private void saveVersionEntry(Note note, int versionNumber) {
        NoteVersion version = NoteVersion.builder()
                .noteId(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .versionNumber(versionNumber)
                .build();
        noteVersionRepository.save(version);
    }
}
