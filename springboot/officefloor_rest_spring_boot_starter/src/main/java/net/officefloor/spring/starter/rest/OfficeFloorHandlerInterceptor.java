package net.officefloor.spring.starter.rest;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.servlet.HttpServletEntityByteSequence;
import net.officefloor.server.http.servlet.HttpServletHttpResponseWriter;
import net.officefloor.server.http.servlet.HttpServletNonMaterialisedHttpHeaders;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;
import net.officefloor.spring.starter.rest.cors.ComposeCorsConfiguration;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OfficeFloorHandlerInterceptor implements HandlerInterceptor {

    /**
     * {@link StreamBufferPool}.
     */
    private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
            () -> ByteBuffer.allocate(1024), 10, 1000);

    private static final CorsProcessor corsProcessor = new DefaultCorsProcessor();

    private final HttpServletOfficeFloorBridge bridge;

    private final Map<String, OfficeFloorRestEndpoint> servicing = new HashMap<>();

    private final ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider;

    private final ObjectProvider<CorsConfigurationSource> corsConfigurationSourceProvider;

    private final ObjectProvider<DispatcherServlet> dispatcherServletProvider;

    private final ObjectProvider<ApplicationContext> applicationContextProvider;

    public OfficeFloorHandlerInterceptor(HttpServletOfficeFloorBridge bridge,
                                         List<OfficeFloorRestEndpoint> restEndpoints,
                                         ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
                                         ObjectProvider<CorsConfigurationSource> corsConfigurationSourceProvider,
                                         ObjectProvider<DispatcherServlet> dispatcherServletProvider,
                                         ObjectProvider<ApplicationContext> applicationContextProvider) {
        this.bridge = bridge;
        this.handlerAdapterProvider = handlerAdapterProvider;
        this.corsConfigurationSourceProvider = corsConfigurationSourceProvider;
        this.dispatcherServletProvider = dispatcherServletProvider;
        this.applicationContextProvider = applicationContextProvider;

        // Build the handling of rest endpoints
        for (OfficeFloorRestEndpoint restEndpoint : restEndpoints) {
            this.servicing.put(restEndpoint.getHttpMethod().getName().toUpperCase(), restEndpoint);
        }
    }


    /*
     * ====================== HandlerInterceptor =========================
     */

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Obtain the handling end point
        OfficeFloorRestEndpoint endpoint = this.servicing.get(request.getMethod().toUpperCase());

        // Determine if CORS request
        String origin = request.getHeader(HttpHeaders.ORIGIN);
        if (origin != null) {

            // Create the CORS configuration
            CorsConfiguration corsConfiguration = new CorsConfiguration();

            // TODO include WebMvcConfigurer CORS Configuration

            // Add possible CORS configuration source
            CorsConfigurationSource corsConfigurationSource = this.corsConfigurationSourceProvider.getIfAvailable();
            if (corsConfigurationSource != null) {
                CorsConfiguration sourceCorsConfiguration = corsConfigurationSource.getCorsConfiguration(request);
                if (sourceCorsConfiguration != null) {
                    corsConfiguration = corsConfiguration.combine(sourceCorsConfiguration);
                }
            }

            // Add OfficeFloor end point configuration
            if (endpoint != null) {
                CorsConfiguration composeCorsConfiguration = endpoint.getCorsConfiguration();
                if (composeCorsConfiguration != null) {
                    corsConfiguration = corsConfiguration.combine(composeCorsConfiguration);
                }
            }

            // Process CORS and determine if handled (rejected)
            if (!corsProcessor.processRequest(corsConfiguration, request, response)) {
                return false; // Handled CORS request
            }
        }

        // Determine if pre-flight CORS request
        if (HttpMethod.OPTIONS.matches(request.getMethod()) &&
                request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD) != null) {
            response.setStatus(HttpServletResponse.SC_OK);
            return false; // Handled pre-flight request
        }

        // Determine if handled
        if (endpoint == null) {
            return true; // skip, not handled by OfficeFloor
        }

        // Service the end point
        ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> input = endpoint.getExternalServiceInput();

        // Obtain the handler adapter
        RequestMappingHandlerAdapter handlerAdapter = this.handlerAdapterProvider.getObject();

        // Obtain the dispatch servlet
        DispatcherServlet dispatcherServlet = this.dispatcherServletProvider.getObject();

        // Obtain the application context
        ApplicationContext applicationContext = this.applicationContextProvider.getObject();

        // Create the request headers
        NonMaterialisedHttpHeaders httpHeaders = new HttpServletNonMaterialisedHttpHeaders(request);

        // Create the entity content
        ByteSequence entity;
        synchronized (request) {
            entity = new HttpServletEntityByteSequence(request);
        }

        // Create the writer of the response
        HttpServletHttpResponseWriter writer;
        synchronized (response) {
            writer = new HttpServletHttpResponseWriter(response, bufferPool);
        }

        // Create the server HTTP connection
        net.officefloor.server.http.HttpMethod httpMethod = net.officefloor.server.http.HttpMethod.getHttpMethod(request.getMethod());
        String requestUri = request.getRequestURI()
                + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
        if (requestUri.startsWith("/")) {
            requestUri = requestUri.substring("/".length());
        }
        final String finalRequestUri = requestUri;
        SpringServerHttpConnection connection = new SpringServerHttpConnection(
                this.bridge.getHttpServerLocation(), request.isSecure(), () -> httpMethod, () -> finalRequestUri,
                HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders, entity, null, null,
                this.bridge.isIncludeEscalationStackTrace(), writer, bufferPool,
                request, response, handler, handlerAdapter, dispatcherServlet, applicationContext);

        // Undertake servicing
        AsyncContext async = request.startAsync();
        input.service(connection, (escalation) -> {
            try {
                connection.getServiceFlowCallback().run(escalation);
            } finally {
                async.complete();
            }
        });

        // Handled
        return false;
    }

}
