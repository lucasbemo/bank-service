package com.lz.bank.adapter.out.http;

import com.lz.bank.domain.port.NotificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

public class NotificationHttpAdapter implements NotificationPort {
    private final RestTemplate restTemplate;
    private final String notifyUrl;

    public NotificationHttpAdapter(RestTemplateBuilder restTemplateBuilder,
                                   @Value("${bank.notify.url}") String notifyUrl) {
        this.restTemplate = restTemplateBuilder.build();
        this.notifyUrl = notifyUrl;
    }

    @Override
    public void notifyTransfer(Long transferId) {
        NotificationRequest request = new NotificationRequest(transferId);
        restTemplate.postForEntity(notifyUrl, request, Void.class);
    }

    private record NotificationRequest(Long transferId) {
    }
}
