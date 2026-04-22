package com.brewnow.service.impl;

import com.brewnow.config.MinioProperties;
import com.brewnow.service.MinioStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioStorageServiceImpl implements MinioStorageService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    private volatile boolean bucketReady = false;

    @Override
    public String uploadImage(MultipartFile file, String folder, String fileName) {
        try {
            return upload(file.getInputStream(), file.getSize(), file.getContentType(), folder, fileName);
        } catch (Exception e) {
            log.error("上传文件到MinIO失败: folder={}, fileName={}", folder, fileName, e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public String uploadBytes(byte[] bytes, String contentType, String folder, String fileName) {
        try {
            return upload(new java.io.ByteArrayInputStream(bytes), bytes.length, contentType, folder, fileName);
        } catch (Exception e) {
            log.error("上传字节流到MinIO失败: folder={}, fileName={}", folder, fileName, e);
            throw new RuntimeException("文件上传失败");
        }
    }

    private String upload(InputStream inputStream, long size, String contentType, String folder, String fileName) {
        if (!minioProperties.isEnabled()) {
            throw new IllegalStateException("MinIO未启用，请检查配置");
        }

        try {
            ensureBucketReady();
            String objectName = normalizeObjectName(folder, fileName);
            try (InputStream stream = inputStream) {
                minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .stream(stream, size, -1)
                            .contentType(contentType)
                            .build()
                );
            }

            return buildPublicUrl(objectName);
        } catch (Exception e) {
            throw new RuntimeException("文件上传失败");
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void initializeBucketOnStartup() {
        if (!minioProperties.isEnabled()) {
            return;
        }

        try {
            ensureBucketReady();
            log.info("MinIO已就绪，Bucket: {}", minioProperties.getBucket());
        } catch (Exception e) {
            log.error("MinIO初始化失败，请检查endpoint、账号密码和Bucket权限配置", e);
        }
    }

    private void ensureBucketReady() throws Exception {
        if (bucketReady) {
            return;
        }

        synchronized (this) {
            if (bucketReady) {
                return;
            }

            String bucket = minioProperties.getBucket();
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!exists) {
                if (!minioProperties.isAutoCreateBucket()) {
                    throw new IllegalStateException("MinIO Bucket不存在: " + bucket);
                }
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("已自动创建MinIO Bucket: {}", bucket);
            }

            if (minioProperties.isPublicRead()) {
                String policy = buildPublicReadPolicy(bucket);
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucket)
                                .config(policy)
                                .build()
                );
            }

            bucketReady = true;
        }
    }

    private String normalizeObjectName(String folder, String fileName) {
        String cleanFolder = StringUtils.trimTrailingCharacter(folder.trim(), '/');
        return cleanFolder + "/" + fileName;
    }

    private String buildPublicUrl(String objectName) {
        String publicEndpoint = StringUtils.hasText(minioProperties.getPublicEndpoint())
                ? minioProperties.getPublicEndpoint()
                : minioProperties.getEndpoint();
        return publicEndpoint.replaceAll("/+$", "")
                + "/" + minioProperties.getBucket()
                + "/" + objectName;
    }

    private String buildPublicReadPolicy(String bucket) {
        return "{\n" +
                "  \"Version\": \"2012-10-17\",\n" +
                "  \"Statement\": [\n" +
                "    {\n" +
                "      \"Effect\": \"Allow\",\n" +
                "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                "      \"Action\": [\"s3:GetObject\"],\n" +
                "      \"Resource\": [\"arn:aws:s3:::" + bucket + "/*\"]\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
}
