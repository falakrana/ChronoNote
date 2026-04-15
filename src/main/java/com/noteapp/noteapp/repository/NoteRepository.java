package com.noteapp.noteapp.repository;

import com.noteapp.noteapp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;  // Add this import

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByDeletedFalse();
    List<Note> findByDeletedTrue();
}
