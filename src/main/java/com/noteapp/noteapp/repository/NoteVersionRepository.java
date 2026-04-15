package com.noteapp.noteapp.repository;

import com.noteapp.noteapp.model.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {
    List<NoteVersion> findByNoteId(Long noteId);
    void deleteByNoteId(Long noteId);
}
