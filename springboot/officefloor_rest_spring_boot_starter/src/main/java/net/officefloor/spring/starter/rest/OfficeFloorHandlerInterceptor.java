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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class OfficeFloorHandlerInterceptor implements HandlerInterceptor {

    /**
     * {@link StreamBufferPool}.
     */
    private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
            () -> ByteBuffer.allocate(1024), 10, 1000);

    private static final CorsProcessor corsProcessor = new DefaultCorsProcessor();

    private final HttpServletOfficeFloorBridge bridge;

    private final Map<String, OfficeFloorRestMethod> servicing = new HashMap<>();

    private final ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider;

    private final ObjectProvider<DispatcherServlet> dispatcherServletProvider;

    private final ObjectProvider<ApplicationContext> applicationContextProvider;

    public OfficeFloorHandlerInterceptor(HttpServletOfficeFloorBridge bridge,
                                         OfficeFloorRestEndpoint restEndpoint,
                                         ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
                                         ObjectProvider<DispatcherServlet> dispatcherServletProvider,
                                         ObjectProvider<ApplicationContext> applicationContextProvider) {
        this.bridge = bridge;
        this.handlerAdapterProvider = handlerAdapterProvider;
        this.dispatcherServletProvider = dispatcherServletProvider;
        this.applicationContextProvider = applicationContextProvider;

        // Build the handling of rest endpoints
        for (OfficeFloorRestMethod restMethod : restEndpoint.getRestMethods()) {
            this.servicing.put(restMethod.getHttpMethod().getName().toUpperCase(), restMethod);
        }
    }


    /*
     * ====================== HandlerInterceptor =========================
     */

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Obtain the handling REST Method
        OfficeFloorRestMethod restMethod = this.servicing.get(request.getMethod().toUpperCase());
        if (restMethod == null) {
            return true; // skip, not handled by OfficeFloor
        }

        // Service the end point
        ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> input = restMethod.getExternalServiceInput();

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
