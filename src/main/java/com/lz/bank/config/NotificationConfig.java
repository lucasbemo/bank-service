package com.lz.bank.config;

import com.lz.bank.adapter.out.http.MockNotificationAdapter;
import com.lz.bank.adapter.out.http.NotificationHttpAdapter;
import com.lz.bank.domain.port.NotificationPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationConfig {
    @Bean
    @ConditionalOnProperty(name = "NOTIFY_MOCK", havingValue = "true")
    public NotificationPort mockNotificationPort() {
        return new MockNotificationAdapter();
    }

    @Bean
    @ConditionalOnMissingBean(NotificationPort.class)
    public NotificationPort notificationPort(RestTemplateBuilder restTemplateBuilder,
                                              @Value("${bank.notify.url}") String notifyUrl) {
        return new NotificationHttpAdapter(restTemplateBuilder, notifyUrl);
    }
}
