package com.noteapp.noteapp.repository;

import com.noteapp.noteapp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTenantIdAndDeletedFalseOrderByUpdatedAtDesc(String tenantId);
    List<Note> findByTenantIdAndDeletedTrueOrderByUpdatedAtDesc(String tenantId);
    Optional<Note> findByIdAndTenantId(Long id, String tenantId);
    boolean existsByIdAndTenantId(Long id, String tenantId);
    void deleteByIdAndTenantId(Long id, String tenantId);
}
