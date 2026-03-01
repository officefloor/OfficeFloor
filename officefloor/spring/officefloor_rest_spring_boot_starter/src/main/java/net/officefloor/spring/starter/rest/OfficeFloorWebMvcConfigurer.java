package net.officefloor.spring.starter.rest;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class OfficeFloorWebMvcConfigurer implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new OfficeFloorHandlerInterceptor()).addPathPatterns("/officefloor");
    }

}
