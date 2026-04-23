package com.noteapp.noteapp.service;

import com.noteapp.noteapp.model.Folder;
import com.noteapp.noteapp.repository.FolderRepository;
import com.noteapp.noteapp.repository.NoteRepository;
import com.noteapp.noteapp.tenant.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final NoteRepository noteRepository;

    @Transactional
    public Folder createFolder(Folder folder) {
        String tenantId = TenantContext.getRequiredTenantId();
        String normalizedName = normalizeName(folder.getName());

        Long parentFolderId = folder.getParentFolderId();
        if (parentFolderId != null && !folderRepository.existsByIdAndTenantId(parentFolderId, tenantId)) {
            throw new RuntimeException("Parent folder not found with id: " + parentFolderId);
        }

        folder.setName(normalizedName);
        folder.setTenantId(tenantId);
        folder.setDeleted(false);
        return folderRepository.save(folder);
    }

    public List<Folder> getAllFolders() {
        return folderRepository.findByTenantIdAndDeletedFalseOrderByCreatedAtAscIdAsc(TenantContext.getRequiredTenantId());
    }

    public List<Folder> getTrashFolders() {
        return folderRepository.findByTenantIdAndDeletedTrueOrderByUpdatedAtDesc(TenantContext.getRequiredTenantId());
    }

    @Transactional
    public Folder updateFolder(Long id, Folder folderDetails) {
        String tenantId = TenantContext.getRequiredTenantId();
        Folder existing = folderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        String normalizedName = normalizeName(folderDetails.getName());
        Long newParentFolderId = folderDetails.getParentFolderId();

        if (newParentFolderId != null) {
            if (id.equals(newParentFolderId)) {
                throw new RuntimeException("A folder cannot be its own parent");
            }
            if (!folderRepository.existsByIdAndTenantId(newParentFolderId, tenantId)) {
                throw new RuntimeException("Parent folder not found with id: " + newParentFolderId);
            }
            ensureNoCycle(id, newParentFolderId, tenantId);
        }

        existing.setName(normalizedName);
        existing.setParentFolderId(newParentFolderId);
        return folderRepository.save(existing);
    }

    /** Soft-delete: folder + all descendants go to trash; their notes go to trash too. */
    @Transactional
    public void softDeleteFolder(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();

        Folder folder = folderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        List<Folder> allFolders = folderRepository.findByTenantIdOrderByCreatedAtAscIdAsc(tenantId);
        Set<Long> subtreeIds = collectSubtreeIds(folder.getId(), allFolders);

        // Soft-delete the notes inside the subtree
        noteRepository.moveToTrashForTenantAndFolderIds(tenantId, subtreeIds);
        // Soft-delete all folders in the subtree
        folderRepository.softDeleteByTenantIdAndIdIn(tenantId, subtreeIds);
    }

    /** Restore a soft-deleted folder and all its descendants (folders and notes). */
    @Transactional
    public void restoreFolder(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();
        Folder folder = folderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        List<Folder> allFolders = folderRepository.findByTenantIdOrderByCreatedAtAscIdAsc(tenantId);
        Set<Long> subtreeIds = collectSubtreeIds(folder.getId(), allFolders);

        // Restore all folders in the subtree
        folderRepository.restoreByTenantIdAndIdIn(tenantId, subtreeIds);
        // Restore all notes in the subtree
        noteRepository.restoreFromTrashForTenantAndFolderIds(tenantId, subtreeIds);
    }

    /** Permanently delete a trashed folder and all its descendants. */
    @Transactional
    public void hardDeleteFolder(Long id) {
        String tenantId = TenantContext.getRequiredTenantId();

        Folder folder = folderRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new RuntimeException("Folder not found with id: " + id));

        List<Folder> allFolders = folderRepository.findByTenantIdOrderByCreatedAtAscIdAsc(tenantId);
        Set<Long> subtreeIds = collectSubtreeIds(folder.getId(), allFolders);

        folderRepository.deleteByTenantIdAndIdIn(tenantId, subtreeIds);
    }

    private void ensureNoCycle(Long folderId, Long candidateParentFolderId, String tenantId) {
        List<Folder> allFolders = folderRepository.findByTenantIdOrderByCreatedAtAscIdAsc(tenantId);
        Map<Long, Long> parentById = allFolders.stream()
                .filter(f -> f.getParentFolderId() != null)
                .collect(Collectors.toMap(Folder::getId, Folder::getParentFolderId));

        Long current = candidateParentFolderId;
        while (current != null) {
            if (folderId.equals(current)) {
                throw new RuntimeException("Cannot move folder into one of its descendants");
            }
            current = parentById.get(current);
        }
    }

    private Set<Long> collectSubtreeIds(Long rootFolderId, List<Folder> allFolders) {
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (Folder folder : allFolders) {
            if (folder.getParentFolderId() != null) {
                childrenByParent
                        .computeIfAbsent(folder.getParentFolderId(), ignored -> new ArrayList<>())
                        .add(folder.getId());
            }
        }

        Set<Long> result = new HashSet<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(rootFolderId);

        while (!stack.isEmpty()) {
            Long current = stack.pop();
            if (result.add(current)) {
                for (Long childId : childrenByParent.getOrDefault(current, Collections.emptyList())) {
                    stack.push(childId);
                }
            }
        }

        return result;
    }

    private String normalizeName(String rawName) {
        if (rawName == null || rawName.trim().isEmpty()) {
            throw new RuntimeException("Folder name is required");
        }
        return rawName.trim();
    }
}
