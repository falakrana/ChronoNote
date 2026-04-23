package com.noteapp.noteapp.repository;

import com.noteapp.noteapp.model.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByTenantIdAndDeletedFalseOrderByUpdatedAtDesc(String tenantId);
    List<Note> findByTenantIdAndDeletedFalseAndFolderIdOrderByUpdatedAtDesc(String tenantId, Long folderId);
    List<Note> findByTenantIdAndDeletedFalseAndFolderIdIsNullOrderByUpdatedAtDesc(String tenantId);
    List<Note> findByTenantIdAndDeletedTrueOrderByUpdatedAtDesc(String tenantId);
    Optional<Note> findByIdAndTenantId(Long id, String tenantId);
    boolean existsByIdAndTenantId(Long id, String tenantId);
    void deleteByIdAndTenantId(Long id, String tenantId);

    @Modifying
    @Query("update Note n set n.folderId = null where n.tenantId = :tenantId and n.folderId in :folderIds")
    int clearFolderForTenantAndFolderIds(@Param("tenantId") String tenantId, @Param("folderIds") Collection<Long> folderIds);

    @Modifying
    @Query("update Note n set n.deleted = true where n.tenantId = :tenantId and n.folderId in :folderIds")
    int moveToTrashForTenantAndFolderIds(@Param("tenantId") String tenantId, @Param("folderIds") Collection<Long> folderIds);

    @Modifying
    @Query("update Note n set n.deleted = false where n.tenantId = :tenantId and n.folderId in :folderIds")
    int restoreFromTrashForTenantAndFolderIds(@Param("tenantId") String tenantId, @Param("folderIds") Collection<Long> folderIds);
}
