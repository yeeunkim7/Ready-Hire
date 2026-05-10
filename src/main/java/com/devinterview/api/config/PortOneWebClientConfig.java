package com.devinterview.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 포트원 REST API 호출용 WebClient 설정.
 */
@Configuration
public class PortOneWebClientConfig {

    @Bean(name = "portOneWebClient")
    public WebClient portOneWebClient(
        WebClient.Builder webClientBuilder,
        @Value("${portone.v2.base-url:https://api.portone.io}") String baseUrl
    ) {
        return webClientBuilder.baseUrl(baseUrl).build();
    }
}
