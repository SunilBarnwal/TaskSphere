package com.tasksphere.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".png", ".jpg", ".jpeg", ".gif", ".webp", ".pdf",".docx");

    private final Path uploadRoot;

    public FileStorageService(@Value("${tasksphere.upload-dir:uploads}") String uploadDir) throws IOException {
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadRoot);
    }

    public String storeCompletionScreenshot(MultipartFile file, Long teamId, Long studentId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("A completion proof file is required.");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().trim();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf(".")).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException(
                    "Upload a valid proof file: PNG, JPG, JPEG, GIF, WEBP, PDF,OR DOCX.");
        }

        String storedName = "team-" + teamId + "-student-" + studentId + "-" + UUID.randomUUID() + extension;
        Path target = uploadRoot.resolve(storedName).normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store the uploaded proof file.", ex);
        }

        return storedName;
    }

    public Path uploadRoot() {
        return uploadRoot;
    }
}
