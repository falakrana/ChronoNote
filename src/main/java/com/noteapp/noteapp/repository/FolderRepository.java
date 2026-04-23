package com.noteapp.noteapp.repository;

import com.noteapp.noteapp.model.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    // Active (non-deleted) folders
    List<Folder> findByTenantIdAndDeletedFalseOrderByCreatedAtAscIdAsc(String tenantId);
    // Trashed folders
    List<Folder> findByTenantIdAndDeletedTrueOrderByUpdatedAtDesc(String tenantId);

    Optional<Folder> findByIdAndTenantId(Long id, String tenantId);
    boolean existsByIdAndTenantId(Long id, String tenantId);

    // For cycle-check and subtree traversal (needs all, including deleted)
    List<Folder> findByTenantIdOrderByCreatedAtAscIdAsc(String tenantId);

    @Modifying
    @Query("update Folder f set f.deleted = true where f.tenantId = :tenantId and f.id in :folderIds")
    void softDeleteByTenantIdAndIdIn(@Param("tenantId") String tenantId, @Param("folderIds") Collection<Long> folderIds);

    @Modifying
    @Query("update Folder f set f.deleted = false where f.tenantId = :tenantId and f.id in :folderIds")
    void restoreByTenantIdAndIdIn(@Param("tenantId") String tenantId, @Param("folderIds") Collection<Long> folderIds);

    @Modifying
    @Query("delete from Folder f where f.tenantId = :tenantId and f.id in :folderIds")
    void deleteByTenantIdAndIdIn(@Param("tenantId") String tenantId, @Param("folderIds") Collection<Long> folderIds);
}
