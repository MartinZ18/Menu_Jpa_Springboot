package com.menujpa.security;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        filterChain = mock(FilterChain.class);
    }

    private MockHttpServletRequest requestA(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setRemoteAddr("10.0.0.1");
        return request;
    }

    @Test
    void doFilter_rutaNoLimitada_siempreDejaPasar() throws Exception {
        for (int i = 0; i < 10; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(requestA("/api/v1/menus"), response, filterChain);
            assertThat(response.getStatus()).isEqualTo(200); // default de MockHttpServletResponse
        }
        verify(filterChain, times(10)).doFilter(any(), any());
    }

    @Test
    void doFilter_dentroDelLimite_dejaPasar() throws Exception {
        for (int i = 0; i < 5; i++) {
            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilter(requestA("/api/v1/auth/login"), response, filterChain);
            assertThat(response.getStatus()).isEqualTo(200);
        }
        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void doFilter_superandoElLimite_devuelve429YNoLlamaAlChain() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilter(requestA("/api/v1/auth/login"), new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletResponse sexta = new MockHttpServletResponse();
        filter.doFilter(requestA("/api/v1/auth/login"), sexta, filterChain);

        assertThat(sexta.getStatus()).isEqualTo(429);
        verify(filterChain, times(5)).doFilter(any(), any());
    }

    @Test
    void doFilter_ipsDistintas_seCuentanPorSeparado() throws Exception {
        for (int i = 0; i < 5; i++) {
            filter.doFilter(requestA("/api/v1/auth/registrarse"), new MockHttpServletResponse(), filterChain);
        }

        MockHttpServletRequest otraIp = new MockHttpServletRequest("POST", "/api/v1/auth/registrarse");
        otraIp.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(otraIp, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain, times(6)).doFilter(any(), any());
    }
}
