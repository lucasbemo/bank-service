package com.lz.bank.adapter.out.http;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Constructor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthorizationHttpAdapterTest {

    @Test
    void returnsTrueWhenAuthorized() throws Exception {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(builder.build()).thenReturn(restTemplate);

        AuthorizationHttpAdapter adapter = new AuthorizationHttpAdapter(builder, "http://auth");

        @SuppressWarnings("unchecked")
        Class<Object> responseClass = (Class<Object>) Class.forName("com.lz.bank.adapter.out.http.AuthorizationHttpAdapter$AuthResponse");
        Constructor<?> ctor = responseClass.getDeclaredConstructor(String.class);
        ctor.setAccessible(true);
        Object response = ctor.newInstance("Autorizado");

        when(restTemplate.getForObject(eq("http://auth"), eq(responseClass))).thenReturn(response);

        assertThat(adapter.authorize()).isTrue();
    }

    @Test
    void returnsFalseWhenResponseNull() throws Exception {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(builder.build()).thenReturn(restTemplate);

        AuthorizationHttpAdapter adapter = new AuthorizationHttpAdapter(builder, "http://auth");

        @SuppressWarnings("unchecked")
        Class<Object> responseClass = (Class<Object>) Class.forName("com.lz.bank.adapter.out.http.AuthorizationHttpAdapter$AuthResponse");
        when(restTemplate.getForObject(eq("http://auth"), eq(responseClass))).thenReturn(null);

        assertThat(adapter.authorize()).isFalse();
    }

    @Test
    void returnsFalseOnException() throws Exception {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        RestTemplate restTemplate = mock(RestTemplate.class);
        when(builder.build()).thenReturn(restTemplate);

        AuthorizationHttpAdapter adapter = new AuthorizationHttpAdapter(builder, "http://auth");

        @SuppressWarnings("unchecked")
        Class<Object> responseClass = (Class<Object>) Class.forName("com.lz.bank.adapter.out.http.AuthorizationHttpAdapter$AuthResponse");
        when(restTemplate.getForObject(anyString(), eq(responseClass))).thenThrow(new RuntimeException("fail"));

        assertThat(adapter.authorize()).isFalse();
    }
}
