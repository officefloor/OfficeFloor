package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.servlet.HttpServletEntityByteSequence;
import net.officefloor.server.http.servlet.HttpServletHttpResponseWriter;
import net.officefloor.server.http.servlet.HttpServletNonMaterialisedHttpHeaders;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ThreadLocalStreamBufferPool;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.ByteBuffer;

public class OfficeFloorHandlerInterceptor implements HandlerInterceptor {

    /**
     * {@link StreamBufferPool}.
     */
    private static final StreamBufferPool<ByteBuffer> bufferPool = new ThreadLocalStreamBufferPool(
            () -> ByteBuffer.allocate(1024), 10, 1000);

    private final HttpServletOfficeFloorBridge bridge;

    public OfficeFloorHandlerInterceptor(HttpServletOfficeFloorBridge bridge) {
        this.bridge = bridge;
    }


    /*
     * ====================== HandlerInterceptor =========================
     */

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // Ensure GET
        if (!request.getMethod().equals(HttpMethod.GET.name())) {
            return true; // skip, not our concern
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

        // Write intercepted response
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write("OfficeFloor");

        // Handled
        return false;
    }

}
