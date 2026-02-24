package com.smartcompany.service;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.smartcompany.dto.PreSignedUrlDto;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Service
public class ProfileImageService {

    // private final S3Client s3Client;
    private final String bucketName;
    private final String region;
    private final String accessKeyId;
    private final String secretAccessKey;

    public ProfileImageService( @Value("${aws.region}") String region,
                                @Value("${aws.s3.bucket-name}") String bucketName,
                                @Value("${aws.access-key-id}") String accessKeyId,
                                @Value("${aws.secret-access-key}") String secretAccessKey) {
        // this.s3Client = S3Client.builder()
        //         .region(Region.of(region))
        //         .credentialsProvider(StaticCredentialsProvider.create(
        //             AwsBasicCredentials.create(accessKeyId, secretAccessKey)
        //         ))
        //         .build();
        this.bucketName = bucketName;
        this.region = region;
        this.accessKeyId = accessKeyId;
        this.secretAccessKey = secretAccessKey;
    }

    // public String uploadFile(MultipartFile file, String key, String username) throws Exception {
    //     String fileExtension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
    //     String uniqueFileName = username + UUID.randomUUID().toString() + fileExtension;
    //     PutObjectRequest request = PutObjectRequest.builder()
    //             .bucket(bucketName)
    //             .key(uniqueFileName)
    //             .build();

    //     s3Client.putObject(request, software.amazon.awssdk.core.sync.RequestBody.fromBytes(file.getBytes()));
    //     return uniqueFileName;
    // }

    public PreSignedUrlDto generatePreSignedPutUrl(String objectKey, String username) {
        S3Presigner presigner = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .build();

        String fileExtension = objectKey.substring(objectKey.lastIndexOf("."));
        String uniqueFileName = username + UUID.randomUUID().toString() + fileExtension;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(uniqueFileName)
                .build();

        PresignedPutObjectRequest presignedPutObjectRequest = presigner.presignPutObject(b -> b
                .signatureDuration(Duration.ofMinutes(15))
                .putObjectRequest(putObjectRequest));

        return new PreSignedUrlDto(presignedPutObjectRequest.url(), uniqueFileName);
    }

    public URL generatePreSignedGetUrl(String key) {
        S3Presigner presigner = S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ))
            .build();

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        PresignedGetObjectRequest presignedGetObjectRequest = presigner.presignGetObject(b -> b
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getObjectRequest));

        return presignedGetObjectRequest.url();
    }
    
}
