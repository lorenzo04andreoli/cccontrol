package com.ConselhoDaComunidade.JudicialControl.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.time.Duration;

@Service
public class S3UrlService {

    private final String bucket;
    private final S3Presigner presigner;

    public S3UrlService(@Value("${aws.s3.bucket}") String bucket,
                        @Value("${aws.region}") String region) {
        this.bucket = bucket;
        this.presigner = S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }

    public String presignedGet(String key, Duration ttl) {
        if (key == null || key.isBlank()) return null;

        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(ttl)
                .getObjectRequest(get)
                .build();

        return presigner.presignGetObject(presign).url().toString();
    }
}
