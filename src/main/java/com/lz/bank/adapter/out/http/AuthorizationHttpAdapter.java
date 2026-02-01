package com.lz.bank.adapter.out.http;

import com.lz.bank.domain.port.AuthorizationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class AuthorizationHttpAdapter implements AuthorizationPort {
    private final RestTemplate restTemplate;
    private final String authUrl;

    public AuthorizationHttpAdapter(RestTemplateBuilder restTemplateBuilder,
                                    @Value("${bank.auth.url}") String authUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.authUrl = authUrl;
    }

    @Override
    public boolean authorize() {
        try {
            AuthResponse response = restTemplate.getForObject(authUrl, AuthResponse.class);
            return response != null && "Autorizado".equalsIgnoreCase(response.message());
        } catch (Exception ex) {
            return false;
        }
    }

    private record AuthResponse(String message) {
    }
}
