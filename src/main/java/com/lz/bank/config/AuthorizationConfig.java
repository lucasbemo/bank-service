package com.lz.bank.config;

import com.lz.bank.adapter.out.http.AuthorizationHttpAdapter;
import com.lz.bank.adapter.out.http.MockAuthorizationAdapter;
import com.lz.bank.domain.port.AuthorizationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AuthorizationConfig {
    @Bean
    @Profile("test")
    @ConditionalOnProperty(name = "AUTH_MOCK", havingValue = "true")
    public AuthorizationPort mockAuthorizationPort() {
        return new MockAuthorizationAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizationPort.class)
    public AuthorizationPort authorizationPort(RestTemplateBuilder restTemplateBuilder,
                                                @Value("${bank.auth.url}") String authUrl) {
        return new AuthorizationHttpAdapter(restTemplateBuilder, authUrl);
    }
}
