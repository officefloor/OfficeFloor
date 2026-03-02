package net.officefloor.spring.starter.rest;

import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class OfficeFloorWebMvcConfigurer implements WebMvcConfigurer {

    private final HttpServletOfficeFloorBridge bridge;

    public OfficeFloorWebMvcConfigurer(HttpServletOfficeFloorBridge bridge) {
        this.bridge = bridge;
    }

    /*
     * ======================= WebMvcConfigurer =====================
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OfficeFloorHandlerInterceptor(this.bridge)).addPathPatterns("/officefloor");
    }

}
