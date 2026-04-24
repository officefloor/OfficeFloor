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

    private final OfficeFloorRestProperties properties;

    private final ConfigurableApplicationContext applicationContext;

    private final ObjectMapper mapper;

    private final ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider;

    private final ObjectProvider<CorsConfigurationSource> corsConfigurationSourceProvider;

    private final ObjectProvider<DispatcherServlet> dispatcherServletProvider;

    private final ObjectProvider<ApplicationContext> applicationContextProvider;

    private List<OfficeFloorRestEndpoint> restEndpoints = new LinkedList<>();

    private HttpServletOfficeFloorBridge bridge;

    private OfficeFloor officeFloor;

    public OfficeFloorWebMvcConfigurer(OfficeFloorRestProperties properties,
                                       ConfigurableApplicationContext applicationContext,
                                       ObjectMapper mapper,
                                       ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
                                       ObjectProvider<CorsConfigurationSource> corsConfigurationSourceProvider,
                                       ObjectProvider<DispatcherServlet> dispatcherServletProvider,
                                       ObjectProvider<ApplicationContext> applicationContextProvider) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.mapper = mapper;
        this.handlerAdapterProvider = handlerAdapterProvider;
        this.corsConfigurationSourceProvider = corsConfigurationSourceProvider;
        this.dispatcherServletProvider = dispatcherServletProvider;
        this.applicationContextProvider = applicationContextProvider;
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (this.officeFloor != null) {

            // Close and clean up
            this.officeFloor.closeOfficeFloor();
            this.officeFloor = null;
            this.bridge = null;
        }
    }

    /**
     * Ensures {@link OfficeFloor} is started.
     */
    private void startOfficeFloor() throws Exception {

        // Determine if already started
        if (this.officeFloor != null) {
            return; // already started
        }

        // Load OfficeFloor (capturing the REST endpoints)
        this.bridge = HttpServletHttpServerImplementation.load(() -> {

            // Compile the OfficeFloor
            OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
            compiler.setOfficeFloorSource(new SpringBootOfficeFloorSource(this.mapper, this.restEndpoints,
                    this.applicationContext));
            Map<String, String> sourceProperties = this.properties.getConfig();
            if (sourceProperties != null) {
                sourceProperties.forEach(compiler::addProperty);
            }
            this.officeFloor = compiler.compile("OfficeFloor");
            this.officeFloor.openOfficeFloor();
        });
    }

    /*
     * ======================= WebMvcConfigurer =====================
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {

            // Ensure OfficeFloor started
            this.startOfficeFloor();

            // Load the interceptors
            for (OfficeFloorRestEndpoint endpoint : this.restEndpoints) {
                registry.addInterceptor(
                                new OfficeFloorHandlerInterceptor(this.bridge, endpoint, this.handlerAdapterProvider,
                                        this.corsConfigurationSourceProvider, this.dispatcherServletProvider,
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
            this.startOfficeFloor();

            // Load CORS
            for (OfficeFloorRestEndpoint endpoint : this.restEndpoints) {
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
