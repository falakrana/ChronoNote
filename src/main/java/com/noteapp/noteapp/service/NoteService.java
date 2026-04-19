package com.noteapp.noteapp.service;

import com.noteapp.noteapp.model.Note;
import com.noteapp.noteapp.model.NoteVersion;
import com.noteapp.noteapp.repository.NoteRepository;
import com.noteapp.noteapp.repository.NoteVersionRepository;
import com.noteapp.noteapp.tenant.TenantContext;
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
        String tenantId = TenantContext.getRequiredTenantId();
        if (note.getDeleted() == null) {
            note.setDeleted(false);
        }
        note.setTenantId(tenantId);
        Note savedNote = noteRepository.save(note);

        // Create initial version 1
        saveVersionEntry(savedNote, 1, tenantId);

        return savedNote;
    }

    @Transactional
    public Note updateNote(Long id, Note noteDetails) {
        String tenantId = TenantContext.getRequiredTenantId();
        Note note = noteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));

        // Check if anything actually changed
        boolean isChanged = !note.getTitle().equals(noteDetails.getTitle()) || 
                           !note.getContent().equals(noteDetails.getContent());

        note.setTitle(noteDetails.getTitle());
        note.setContent(noteDetails.getContent());
        Note updatedNote = noteRepository.save(note);

        // Only create a new version if there were actual changes
        if (isChanged) {
            int nextVersion = (int) noteVersionRepository.countByNoteIdAndTenantId(id, tenantId) + 1;
            saveVersionEntry(updatedNote, nextVersion, tenantId);
        }

        return updatedNote;
    }


    public List<Note> getAllNotes() {
        return noteRepository.findByTenantIdAndDeletedFalseOrderByUpdatedAtDesc(TenantContext.getRequiredTenantId());
    }

    public List<Note> getTrashNotes() {
        return noteRepository.findByTenantIdAndDeletedTrueOrderByUpdatedAtDesc(TenantContext.getRequiredTenantId());
    }

    public Note getNoteById(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();
        return noteRepository.findByIdAndTenantId(id, tenantId)
                .filter(note -> note.getDeleted() == null || !note.getDeleted())
                .orElseThrow(() -> new RuntimeException("Note not found or is in trash with id: " + id));
    }

    @Transactional
    public void softDelete(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();
        Note note = noteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
        note.setDeleted(true);
        noteRepository.save(note);
    }

    @Transactional
    public void restoreNote(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();
        Note note = noteRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Note not found with id: " + id));
        note.setDeleted(false);
        noteRepository.save(note);
    }

    @Transactional
    public void hardDelete(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();
        if (!noteRepository.existsByIdAndTenantId(id, tenantId)) {
            throw new RuntimeException("Note not found with id: " + id);
        }
        // Delete history first (Cascade)
        noteVersionRepository.deleteByNoteIdAndTenantId(id, tenantId);
        // Delete note
        noteRepository.deleteByIdAndTenantId(id, tenantId);
    }

    public List<NoteVersion> getNoteHistory(Long noteId) {
        String tenantId = TenantContext.getRequiredTenantId();
        // History should be accessible even if note is soft-deleted for auditing
        if (!noteRepository.existsByIdAndTenantId(noteId, tenantId)) {
            throw new RuntimeException("Note not found with id: " + noteId);
        }
        return noteVersionRepository.findByNoteIdAndTenantIdOrderByVersionNumberDesc(noteId, tenantId);
    }

    private void saveVersionEntry(Note note, int versionNumber, String tenantId) {
        NoteVersion version = NoteVersion.builder()
                .noteId(note.getId())
                .tenantId(tenantId)
                .title(note.getTitle())
                .content(note.getContent())
                .versionNumber(versionNumber)
                .build();
        noteVersionRepository.save(version);
    }
}
