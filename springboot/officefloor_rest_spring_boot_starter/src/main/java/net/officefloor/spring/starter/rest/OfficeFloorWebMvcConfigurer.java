package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import net.officefloor.web.rest.build.RestEndpoint;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OfficeFloorWebMvcConfigurer implements WebMvcConfigurer {

    private final OfficeFloorRestSpringBootStarter starter;

    private final ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider;

    private final ObjectProvider<DispatcherServlet> dispatcherServletProvider;

    private final ObjectProvider<ApplicationContext> applicationContextProvider;

    public OfficeFloorWebMvcConfigurer(OfficeFloorRestSpringBootStarter starter,
                                       ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
                                       ObjectProvider<DispatcherServlet> dispatcherServletProvider,
                                       ObjectProvider<ApplicationContext> applicationContextProvider) {
        this.starter = starter;
        this.handlerAdapterProvider = handlerAdapterProvider;
        this.dispatcherServletProvider = dispatcherServletProvider;
        this.applicationContextProvider = applicationContextProvider;
    }

    /*
     * ======================= WebMvcConfigurer =====================
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {

            // Ensure OfficeFloor started
            this.starter.startOfficeFloor();

            // Load the interceptors
            for (OfficeFloorRestEndpoint endpoint : this.starter.getRestEndpoints()) {
                registry.addInterceptor(
                                new OfficeFloorHandlerInterceptor(this.starter.getBridge(), endpoint,
                                        this.handlerAdapterProvider, this.dispatcherServletProvider,
                                        this.applicationContextProvider))
                        .addPathPatterns(endpoint.getPath());
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        try {

            // Ensure OfficeFloor started
            this.starter.startOfficeFloor();

            // Load CORS
            for (OfficeFloorRestEndpoint endpoint : this.starter.getRestEndpoints()) {
                CorsConfiguration cors = endpoint.getCorsConfiguration();
                if (cors != null) {
                    registry.addMapping(endpoint.getPath()).combine(cors);
                }
            }

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
