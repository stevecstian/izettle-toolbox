package com.izettle.dropwizard.filters;

import com.google.common.net.HttpHeaders;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.http.HttpStatus;

/**
 * Allow access to /admin from localhost and from load balancer but not via a LB (from the outside)
 * by denying all access where header X-ForwardedFor is set.
 *
 * This filter is required when running a DropWizard app on a single port for with:
 *
 * type: simple
 * adminContextPath: /admin
 *
 */
public class AdminDirectAccessFilter implements Filter {

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
                httpResponse.setStatus(HttpStatus.UNAUTHORIZED_401);
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
        return request.getContextPath().startsWith("/admin");
    }

    boolean isDirectAccess(HttpServletRequest request) {
        return request.getHeader(HttpHeaders.X_FORWARDED_FOR) == null;
    }
}
