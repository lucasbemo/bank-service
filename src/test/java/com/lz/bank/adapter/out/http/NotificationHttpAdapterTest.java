package com.lz.bank.adapter.out.http;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationHttpAdapterTest {

    @Test
    void postsNotificationRequest() throws Exception {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(builder.build()).thenReturn(restTemplate);

        NotificationHttpAdapter adapter = new NotificationHttpAdapter(builder, "http://notify");

        adapter.notifyTransfer(42L);

        ArgumentCaptor<Object> requestCaptor = ArgumentCaptor.forClass(Object.class);
        verify(restTemplate).postForEntity(eq("http://notify"), requestCaptor.capture(), eq(Void.class));

        Object request = requestCaptor.getValue();
        Method transferId = request.getClass().getDeclaredMethod("transferId");
        transferId.setAccessible(true);
        assertThat(transferId.invoke(request)).isEqualTo(42L);
    }
}
