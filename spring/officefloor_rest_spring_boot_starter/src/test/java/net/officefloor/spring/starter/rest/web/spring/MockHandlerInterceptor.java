package net.officefloor.spring.starter.rest.web.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class MockHandlerInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Ensure GET
        if (!request.getMethod().equals(HttpMethod.GET.name())) {
            return true; // skip, not our concern
        }

        // Write intercepted response
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("Intercepted");

        // Handled
        return false;
    }
}
