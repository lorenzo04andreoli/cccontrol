package com.ConselhoDaComunidade.JudicialControl.service.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3ProfilePhotoStorage implements ProfilePhotosStorage {

    private final S3Client s3;
    private final String bucket;
    private final ProfilePhotoValidator validator;
    private final S3Presigner presigner;

    public S3ProfilePhotoStorage(
            S3Client s3,
            ProfilePhotoValidator validator,
            @Value("${aws.s3.bucket}") String bucket,
            @Value("${aws.region}") String region
    ) {
        this.s3 = s3;
        this.bucket = bucket;
        this.validator = validator;
        this.presigner = S3Presigner.builder().region(Region.of(region)).build();
    }

    @Override
    public String uploadUserProfilePhoto(Long userId, MultipartFile file) throws IOException {
        validator.validate(file);

        String ext = validator.extensionFromContentType(file.getContentType());
        String key = "users/" + userId + "/profile." + UUID.randomUUID() + "." + ext;

        PutObjectRequest put = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();

        s3.putObject(put, RequestBody.fromBytes(file.getBytes()));
        return key;
    }

    @Override
    public String resolvePhotoUrl(String key) {
        if (key == null || key.isBlank()) return null;

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(get)
                .build();

        return presigner.presignGetObject(presign).url().toString();
    }
}
