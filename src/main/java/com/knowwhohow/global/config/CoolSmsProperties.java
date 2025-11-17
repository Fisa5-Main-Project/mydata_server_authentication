package com.knowwhohow.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "coolsms")
@Getter
@Setter
public class CoolSmsProperties {
    private String apiKey;
    private String apiSecret;
    private String fromNumber;
}
