package com.izettle.dropwizard.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allow access to admin resources from localhost and from load balancer but not via a LB (from the outside)
 * by denying all access where header X-ForwardedFor is set.
 *
 * This filter is required when running a Dropwizard app on a single port (e.g. when 'server.type' is set to 'simple').
 *
 */
public class AdminDirectAccessFilter implements Filter {
    static final int UNAUTHORIZED_401 = 401;
    static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final String adminPath;


    public AdminDirectAccessFilter() {
        // In order to be able to initialize using ServletContextHandler.addFilter(Class, String, EnumSet).
        this.adminPath = "/system";
    }

    public AdminDirectAccessFilter(String adminPath) {
        this.adminPath = adminPath;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            if (isAdmin(httpRequest) && !isDirectAccess(httpRequest)) {

                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(UNAUTHORIZED_401);
                httpResponse.getWriter().print("401 Unauthorized");
            } else {
                chain.doFilter(request, response); // This signals that the request should pass this filter
            }
        }
    }

    @Override
    public void destroy() {

    }

    boolean isAdmin(HttpServletRequest request) {
        return request.getContextPath().startsWith(adminPath);
    }

    boolean isDirectAccess(HttpServletRequest request) {
        return request.getHeader(X_FORWARDED_FOR) == null;
    }
}
