package net.officefloor.spring.starter.rest;

import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OfficeFloorWebMvcConfigurer implements WebMvcConfigurer {

    private final HttpServletOfficeFloorBridge bridge;

    private final List<OfficeFloorRestEndpoint> restEndpoints;

    public OfficeFloorWebMvcConfigurer(HttpServletOfficeFloorBridge bridge, List<OfficeFloorRestEndpoint> restEndpoints) {
        this.bridge = bridge;
        this.restEndpoints = restEndpoints;
    }

    /*
     * ======================= WebMvcConfigurer =====================
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Load the interceptors
        for (OfficeFloorRestEndpoint restEndpoint : this.restEndpoints) {

            // Determine the path
            String path = restEndpoint.getPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            // Register handling by OfficeFloor
            registry.addInterceptor(new OfficeFloorHandlerInterceptor(this.bridge, Collections.singletonList(restEndpoint))).addPathPatterns(path);
        }
    }

}
