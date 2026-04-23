package com.noteapp.noteapp.controller;

import com.noteapp.noteapp.model.Folder;
import com.noteapp.noteapp.service.FolderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class FolderController {

    private final FolderService folderService;

    @PostMapping
    public ResponseEntity<Folder> createFolder(@RequestBody Folder folder) {
        return ResponseEntity.ok(folderService.createFolder(folder));
    }

    @GetMapping
    public List<Folder> getAllFolders() {
        return folderService.getAllFolders();
    }

    @GetMapping("/trash")
    public List<Folder> getTrashFolders() {
        return folderService.getTrashFolders();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Folder> updateFolder(@PathVariable Long id, @RequestBody Folder folderDetails) {
        return ResponseEntity.ok(folderService.updateFolder(id, folderDetails));
    }

    /** Soft-delete — folder + descendants go to trash */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long id) {
        folderService.softDeleteFolder(id);
        return ResponseEntity.noContent().build();
    }

    /** Restore a trashed folder */
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreFolder(@PathVariable Long id) {
        folderService.restoreFolder(id);
        return ResponseEntity.ok().build();
    }

    /** Permanently delete a trashed folder */
    @DeleteMapping("/{id}/permanent")
    public ResponseEntity<Void> hardDeleteFolder(@PathVariable Long id) {
        folderService.hardDeleteFolder(id);
        return ResponseEntity.noContent().build();
    }
}
