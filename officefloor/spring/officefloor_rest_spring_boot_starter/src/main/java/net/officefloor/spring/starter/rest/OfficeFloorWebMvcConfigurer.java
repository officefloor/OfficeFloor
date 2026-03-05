package net.officefloor.spring.starter.rest;

import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.*;

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

        // Create listing of end points
        Map<String, List<OfficeFloorRestEndpoint>> endpointsByPath = new HashMap<>();
        for (OfficeFloorRestEndpoint restEndpoint : this.restEndpoints) {

            // Determine the path
            String path = restEndpoint.getPath();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            // Add handling for HTTP method to path
            List<OfficeFloorRestEndpoint> pathEndpoints = endpointsByPath.computeIfAbsent(path, k -> new ArrayList<>());
            pathEndpoints.add(restEndpoint);
        }

        // Load the interceptors
        endpointsByPath.forEach((path, endpoints) -> {
            registry.addInterceptor(new OfficeFloorHandlerInterceptor(this.bridge, endpoints)).addPathPatterns(path);
        });
    }

}
