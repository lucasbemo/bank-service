package com.lz.bank.adapter.out.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockAuthorizationAdapterTest {

    @Test
    void alwaysAuthorizes() {
        MockAuthorizationAdapter adapter = new MockAuthorizationAdapter();

        assertThat(adapter.authorize()).isTrue();
    }
}
