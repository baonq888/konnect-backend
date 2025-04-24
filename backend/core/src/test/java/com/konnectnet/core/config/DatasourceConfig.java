package com.konnectnet.core.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DatasourceConfig {
    private String url;
    private String username;
    private String password;
}
