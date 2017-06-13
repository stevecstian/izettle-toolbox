package com.izettle.dropwizard.filters;

import static com.izettle.dropwizard.filters.AdminDirectAccessFilter.UNAUTHORIZED_401;
import static com.izettle.dropwizard.filters.AdminDirectAccessFilter.X_FORWARDED_FOR;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Test;

public class AdminDirectAccessFilterTest {

    @Test
    public void adminDirectAccessFilterShouldBlockForwardedTrafficOnAdmin() throws Exception {
        AdminDirectAccessFilter adaFilter = new AdminDirectAccessFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);
        PrintWriter respWriter = mock(PrintWriter.class);

        when(req.getRemoteAddr()).thenReturn("11.22.22.33");
        when(req.getContextPath()).thenReturn("/system");
        when(req.getHeader(X_FORWARDED_FOR)).thenReturn("1.2.3.4");
        when(resp.getWriter()).thenReturn(respWriter);

        adaFilter.doFilter(req, resp, null);
        verify(resp).setStatus(UNAUTHORIZED_401);
        verify(respWriter).print("401 Unauthorized");
    }

    @Test
    public void adminDirectAccessFilterShouldAllowNonForwardedTrafficOnAdmin() throws Exception {
        AdminDirectAccessFilter adaFilter = new AdminDirectAccessFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        FilterChain nextFilter = mock(FilterChain.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getContextPath()).thenReturn("/system");
        when(req.getHeader(X_FORWARDED_FOR)).thenReturn(null);

        adaFilter.doFilter(req, resp, nextFilter);
        verify(nextFilter).doFilter(req, resp);
    }

    @Test
    public void adminDirectAccessFilterShouldAllowNonAdminTraffic() throws Exception {
        AdminDirectAccessFilter adaFilter = new AdminDirectAccessFilter();
        HttpServletRequest req = mock(HttpServletRequest.class);
        FilterChain nextFilter = mock(FilterChain.class);
        HttpServletResponse resp = mock(HttpServletResponse.class);

        when(req.getContextPath()).thenReturn("/something_else");
        when(req.getHeader(X_FORWARDED_FOR)).thenReturn("1.2.3.4");

        adaFilter.doFilter(req, resp, nextFilter);
        verify(nextFilter).doFilter(req, resp);
    }
}
