package com.ConselhoDaComunidade.JudicialControl.service.storage;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Component
public class ProfilePhotoValidator {

    private static final Set<String> ALLOWED = Set.of("image/png", "image/jpeg", "image/webp");
    private static final long MAX = 3L * 1024 * 1024;

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (!ALLOWED.contains(file.getContentType())) {
            throw new IllegalArgumentException("File type not allowed");
        }
        if (file.getSize() > MAX) {
            throw new IllegalArgumentException("File size exceeds limit of 3MB");
        }
    }

    public String extensionFromContentType(String contentType) {
        if (contentType == null) return "jpg";
        return switch (contentType) {
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };
    }
}
