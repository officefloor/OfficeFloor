package net.officefloor.spring.starter.rest;

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
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.expression.ExpressionUtils;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final HttpServletOfficeFloorBridge bridge;

    private final Map<String, ExternalServiceInput<ServerHttpConnection, ProcessAwareServerHttpConnectionManagedObject<ByteBuffer>>> servicing = new HashMap<>();

    private final ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider;

    private final ObjectProvider<DispatcherServlet> dispatcherServletProvider;

    private final ObjectProvider<ApplicationContext> applicationContextProvider;

    public OfficeFloorHandlerInterceptor(HttpServletOfficeFloorBridge bridge,
                                         List<OfficeFloorRestEndpoint> restEndpoints,
                                         ObjectProvider<RequestMappingHandlerAdapter> handlerAdapterProvider,
                                         ObjectProvider<DispatcherServlet> dispatcherServletProvider,
                                         ObjectProvider<ApplicationContext> applicationContextProvider) {
        this.bridge = bridge;
        this.handlerAdapterProvider = handlerAdapterProvider;
        this.dispatcherServletProvider = dispatcherServletProvider;
        this.applicationContextProvider = applicationContextProvider;

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

        // Obtain the handler adapter
        RequestMappingHandlerAdapter handlerAdapter = this.handlerAdapterProvider.getObject();

        // Obtain the dispatch servlet
        DispatcherServlet dispatcherServlet = this.dispatcherServletProvider.getObject();

        // Obtain the application context
        ApplicationContext applicationContext = this.applicationContextProvider.getObject();

        // Obtain the security configuration
        Object configuration = applicationContext.getBean("_prePostMethodSecurityConfiguration");
        Field expressionHandlerField = configuration.getClass().getDeclaredField("expressionHandler");
        expressionHandlerField.setAccessible(true);
        MethodSecurityExpressionHandler expressionHandler = (MethodSecurityExpressionHandler) expressionHandlerField.get(configuration);

        // Handle authentication
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EvaluationContext ctx = expressionHandler.createEvaluationContext(() -> auth, new MethodInvocation() {
            @Override
            public Method getMethod() {
                try {
                    return Object.class.getMethod("hashCode");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public Object[] getArguments() {
                return new Object[0];
            }

            @Override
            public Object proceed() throws Throwable {
                return null;
            }

            @Override
            public Object getThis() {
                return this;
            }

            @Override
            public AccessibleObject getStaticPart() {
                return null;
            }
        });
        Expression expression = expressionHandler.getExpressionParser().parseExpression("hasRole('ADMIN')");
        boolean isPermitted = ExpressionUtils.evaluateAsBoolean(expression, ctx);
        if (!isPermitted) {
            throw new AccessDeniedException("Access denied");
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
        input.service(connection, connection.getServiceFlowCallback());

        // Handled
        return false;
    }

}
