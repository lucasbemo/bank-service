package com.lz.bank.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    void exposesCodeAndMessage() {
        DomainException exception = new DomainException("CODE", "Something happened");

        assertThat(exception.code()).isEqualTo("CODE");
        assertThat(exception.getMessage()).isEqualTo("Something happened");
    }
}
