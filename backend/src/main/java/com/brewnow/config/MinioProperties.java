package com.brewnow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private boolean enabled = true;

    private String endpoint;

    private String publicEndpoint;

    private String accessKey;

    private String secretKey;

    private String bucket = "brew-now";

    private boolean autoCreateBucket = true;

    private boolean publicRead = true;
}
