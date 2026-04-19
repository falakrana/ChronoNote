package com.noteapp.noteapp.repository;

import com.noteapp.noteapp.model.NoteVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteVersionRepository extends JpaRepository<NoteVersion, Long> {
    List<NoteVersion> findByNoteIdAndTenantIdOrderByVersionNumberDesc(Long noteId, String tenantId);
    void deleteByNoteIdAndTenantId(Long noteId, String tenantId);
    long countByNoteIdAndTenantId(Long noteId, String tenantId);
}
