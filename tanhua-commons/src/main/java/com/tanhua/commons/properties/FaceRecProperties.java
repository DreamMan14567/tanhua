package com.tanhua.commons.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author user_Chubby
 * @date 2021/5/4
 * @Description
 */
@Data
@ConfigurationProperties(prefix = "tanhua.face")
public class FaceRecProperties {
    private String appId;
    private String apiKey;
    private String secretKey;
}


