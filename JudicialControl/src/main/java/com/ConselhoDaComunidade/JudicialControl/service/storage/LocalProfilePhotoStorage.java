package com.ConselhoDaComunidade.JudicialControl.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalProfilePhotoStorage implements ProfilePhotosStorage {

    private final ProfilePhotoValidator validator;
    private final Path rootDir;

    public LocalProfilePhotoStorage(ProfilePhotoValidator validator,
                                    @Value("${storage.local.root:uploads}") String root) {
        this.validator = validator;
        this.rootDir = Paths.get(root).toAbsolutePath().normalize();
    }

    @Override
    public String uploadUserProfilePhoto(Long userId, MultipartFile file) throws IOException {
        validator.validate(file);

        String ext = validator.extensionFromContentType(file.getContentType());
        String relPath = "users/" + userId + "/profile." + UUID.randomUUID() + "." + ext;

        Path target = rootDir.resolve(relPath).normalize();
        Files.createDirectories(target.getParent());

        try (var in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        return relPath;
    }

    @Override
    public String resolvePhotoUrl(String storedRelPath) {
        if (storedRelPath == null || storedRelPath.isBlank()) return null;
        return "/usuario/foto/view?path=" + storedRelPath;
    }

    public Path resolveOnDisk(String storedRelPath) {
        return rootDir.resolve(storedRelPath).normalize();
    }

    public Path getRootDir() {
        return rootDir;
    }
}
