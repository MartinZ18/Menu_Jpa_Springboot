package com.menujpa;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class SmokeTest {

    @Test
    void junitYMockitoFuncionan() {
        Runnable mockRunnable = mock(Runnable.class);
        assertThat(mockRunnable).isNotNull();
    }
}
