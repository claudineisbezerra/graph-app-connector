package com.ebao.graphconnector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "graph")
public class SecurityProperties {
    private String instance;
    private String tenantId;
    private String clientId;
    private String secret;
    private String scopes;
    private String teamId;
    private String channelId;
    private String teamName;
    private String channelName;

}
