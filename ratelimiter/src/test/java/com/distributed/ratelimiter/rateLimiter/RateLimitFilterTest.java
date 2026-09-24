package com.distributed.ratelimiter.rateLimiter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.distributed.ratelimiter.config.AppDiagnosticsProperties;
import com.distributed.ratelimiter.config.AppInstanceProperties;
import com.distributed.ratelimiter.config.InstanceIdHeaderFilter;
import com.distributed.ratelimiter.security.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RateLimitFilterTest {

    @Test
    void rateLimitedResponsesIncludeInstanceHeader() throws Exception {
        RedisDistributedRateLimiter rateLimiter = mock(RedisDistributedRateLimiter.class);
        when(rateLimiter.tryConsumeForIp(anyString(), anyString())).thenReturn(false);

        RateLimitFilter filter = new RateLimitFilter(
                rateLimiter,
                mock(JwtService.class),
                new AppInstanceProperties("instance-42"),
                new AppDiagnosticsProperties());

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/auth/login");
        request.addHeader("X-Forwarded-For", "203.0.113.55");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        filter.doFilter(request, response, chain);

        assertEquals(429, response.getStatus());
        assertEquals("instance-42", response.getHeader(InstanceIdHeaderFilter.HEADER_NAME));
        String body = response.getContentAsString();
        assertTrue(body.contains("rate_limit_exceeded"));
        assertTrue(body.contains("instance-42"));
    }
}
