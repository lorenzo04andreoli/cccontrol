package com.ConselhoDaComunidade.JudicialControl.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class S3StorageService {

    private final S3Client s3;

    private final String bucket;

    private static final Set<String> ALLOWED = Set.of("image/png", "image/jpeg");

    public S3StorageService(S3Client s3,
                            @Value("${aws.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    public String uploadUserProfilePhoto(Long userId, MultipartFile file) throws IOException{
        if (file == null || file.isEmpty()){
            throw new IllegalArgumentException("File is empty");
        }
        if (!ALLOWED.contains(file.getContentType())){
            throw new IllegalArgumentException("File type not allowed");
        }
        if (file.getSize() > 3 * 1024 * 1024){
            throw new IllegalArgumentException("File size exceeds limit of 3MB");
        }

        String ext = switch (file.getContentType()){
            case "image/png" -> "png";
            case "image/webp" -> "webp";
            default -> "jpg";
        };

        String key =  "users/" + userId + "/profile." + UUID.randomUUID() + "." + ext;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3.putObject(put, RequestBody.fromBytes(file.getBytes()));
        return key;
    }

}
