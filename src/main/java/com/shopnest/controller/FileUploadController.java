package com.shopnest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/upload")
@Slf4j
public class FileUploadController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * Upload product image — ADMIN only
     * Returns the image URL to store in product.imageUrl
     */
    @PostMapping("/image")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {

        // Validate file
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "No file selected"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Only image files allowed"));
        }

        // Max 5MB
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File too large. Max 5MB"));
        }

        try {
            // Create uploads directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename — avoids overwriting
            String originalName = file.getOriginalFilename();
            String extension = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".jpg";
            String fileName = UUID.randomUUID().toString() + extension;

            // Save file
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = baseUrl + "/api/images/" + fileName;
            log.info("Image uploaded: {}", fileName);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Image uploaded successfully",
                "imageUrl", imageUrl,
                "fileName", fileName
            ));

        } catch (IOException e) {
            log.error("Failed to upload image: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Upload failed: " + e.getMessage()));
        }
    }
}
