package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.server.http.HttpMethod;
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
import org.springframework.web.servlet.HandlerInterceptor;

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

    private final HttpServletOfficeFloorBridge bridge;

    private final Map<String, ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>> servicing = new HashMap<>();

    public OfficeFloorHandlerInterceptor(HttpServletOfficeFloorBridge bridge, List<OfficeFloorRestEndpoint> restEndpoints) {
        this.bridge = bridge;

        // Build the handling of rest endpoints
        for (OfficeFloorRestEndpoint restEndpoint : restEndpoints) {
            this.servicing.put(restEndpoint.getHttpMethod().getName().toUpperCase(), restEndpoint.getExternalServiceInput());
        }
    }


    /*
     * ====================== HandlerInterceptor =========================
     */

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Obtain the handling
        ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>> input = this.servicing.get(request.getMethod().toUpperCase());
        if (input == null) {
            return true; // skip, not handled by OfficeFloor
        }

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
        ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> connection = new ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>(
                this.bridge.getHttpServerLocation(), request.isSecure(), () -> httpMethod, () -> requestUri,
                HttpVersion.getHttpVersion(request.getProtocol()), httpHeaders, entity, null, null,
                this.bridge.isIncludeEscalationStackTrace(), writer, bufferPool);

        // Undertake servicing
        input.service(connection, connection.getServiceFlowCallback());

        // Handled
        return false;
    }

}
