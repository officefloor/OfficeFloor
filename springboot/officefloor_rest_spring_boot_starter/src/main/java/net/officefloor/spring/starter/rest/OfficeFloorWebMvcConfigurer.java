package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
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
            this.officeFloor.closeOfficeFloor();
        }
    }

    /*
     * ======================= WebMvcConfigurer =====================
     */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        try {

            // Load OfficeFloor (capturing the REST endpoints)
            List<OfficeFloorRestEndpoint> restEndpoints = new ArrayList<>();
            HttpServletOfficeFloorBridge bridge = HttpServletHttpServerImplementation.load(() -> {

                // Compile the OfficeFloor
                OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
                compiler.setOfficeFloorSource(new SpringBootOfficeFloorSource(this.mapper, restEndpoints, this.applicationContext));
                Map<String, String> sourceProperties = this.properties.getConfig();
                if (sourceProperties != null) {
                    sourceProperties.forEach(compiler::addProperty);
                }
                this.officeFloor = compiler.compile("OfficeFloor");
                this.officeFloor.openOfficeFloor();
            });

            // Create listing of end points
            Map<String, List<OfficeFloorRestEndpoint>> endpointsByPath = new HashMap<>();
            for (OfficeFloorRestEndpoint restEndpoint : restEndpoints) {

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
                registry.addInterceptor(
                        new OfficeFloorHandlerInterceptor(bridge, endpoints, this.handlerAdapterProvider,
                                this.corsConfigurationSourceProvider, this.dispatcherServletProvider,
                                this.applicationContextProvider))
                        .addPathPatterns(path);
            });

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
