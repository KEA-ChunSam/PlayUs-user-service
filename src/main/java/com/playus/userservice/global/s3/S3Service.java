package com.playus.userservice.global.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    // String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    public void deleteImage(String imageFileName) {

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(imageFileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);

        log.info("S3에서 이미지 삭제 완료: {}", imageFileName);
    }

    public String generatePresignedUrl(String imageFileName) {

        PutObjectRequest objectRequest = createPutObjectRequest(imageFileName);
        PutObjectPresignRequest presignRequest = createPresignedRequest(objectRequest, 10);

        return s3Presigner.presignPutObject(presignRequest).url().toString();
    }

    private PutObjectRequest createPutObjectRequest(String imageFileName) {
        return PutObjectRequest.builder()
                .bucket(bucketName)
                .key(imageFileName)
                .build();
    }

    private static PutObjectPresignRequest createPresignedRequest(PutObjectRequest objectRequest, int minutes) {
        return PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(minutes))
                .putObjectRequest(objectRequest)
                .build();
    }
}
