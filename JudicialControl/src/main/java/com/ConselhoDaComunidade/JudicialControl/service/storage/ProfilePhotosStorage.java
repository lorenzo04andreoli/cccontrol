package com.ConselhoDaComunidade.JudicialControl.service.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProfilePhotosStorage {

    String uploadUserProfilePhoto(Long userId, MultipartFile file) throws IOException;

    String resolvePhotoUrl(String storedKeyOrPath);
}
