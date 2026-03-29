package net.officefloor.spring.starter.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SpringServerHttpConnection extends ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> {

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final Object handler;

    private final RequestMappingHandlerAdapter handlerAdapter;

    private final DispatcherServlet dispatcherServlet;

    private final ApplicationContext applicationContext;

    private final ModelAndViewContainer mavContainer;

    private final NativeWebRequest webRequest;

    private final Map<Method, ModelAndViewBridge> modelAndViewBridges = new HashMap<>();

    private ModelAndViewBridge lastModelAndView = null;

    /**
     * Instantiate.
     *
     * @param serverLocation                  {@link HttpServerLocation}.
     * @param isSecure                        Indicates if secure.
     * @param methodSupplier                  {@link Supplier} for the
     *                                        {@link HttpRequest}
     *                                        {@link HttpMethod}.
     * @param requestUriSupplier              {@link Supplier} for the
     *                                        {@link HttpRequest} URI.
     * @param version                         {@link HttpVersion} for the
     *                                        {@link HttpRequest}.
     * @param requestHeaders                  {@link NonMaterialisedHttpHeaders} for
     *                                        the {@link HttpRequest}.
     * @param requestEntity                   {@link ByteSequence} for the
     *                                        {@link HttpRequest} entity.
     * @param serverName                      Name of the server. May be
     *                                        <code>null</code> if not sending
     *                                        <code>Server</code>
     *                                        {@link HttpHeader}.
     * @param dateHttpHeaderClock             {@link DateHttpHeaderClock}. May be
     *                                        <code>null</code> to not send
     *                                        <code>Date</code> {@link HttpHeader}.
     * @param isIncludeStackTraceOnEscalation <code>true</code> to include the
     *                                        {@link Escalation} stack trace in the
     *                                        {@link HttpResponse}.
     * @param writer                          {@link HttpResponseWriter}.
     * @param bufferPool                      {@link StreamBufferPool}.
     */
    public SpringServerHttpConnection(HttpServerLocation serverLocation,
                                      boolean isSecure, Supplier<HttpMethod> methodSupplier, Supplier<String> requestUriSupplier,
                                      HttpVersion version, NonMaterialisedHttpHeaders requestHeaders, ByteSequence requestEntity,
                                      HttpHeaderValue serverName, DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeStackTraceOnEscalation,
                                      HttpResponseWriter<ByteBuffer> writer, StreamBufferPool<ByteBuffer> bufferPool,
                                      HttpServletRequest request, HttpServletResponse response, Object handler,
                                      RequestMappingHandlerAdapter handlerAdapter, DispatcherServlet dispatcherServlet, ApplicationContext applicationContext) {
        super(serverLocation, isSecure, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity, serverName, dateHttpHeaderClock, isIncludeStackTraceOnEscalation, writer, bufferPool);
        this.request = request;
        this.response = response;
        this.handler = handler;
        this.handlerAdapter = handlerAdapter;
        this.dispatcherServlet = dispatcherServlet;
        this.applicationContext = applicationContext;

        // Create the additional state
        this.mavContainer = new ModelAndViewContainer();
        this.webRequest = new ServletWebRequest(request, response);
    }

    /**
     * Obtains the {@link HttpServletRequest}.
     *
     * @return {@link HttpServletRequest}.
     */
    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }

    /**
     * Obtains the {@link HttpServletResponse}.
     *
     * @return {@link HttpServletResponse}.
     */
    public HttpServletResponse getHttpServletResponse() {
        return this.response;
    }

    /**
     * Obtains the Spring handler.
     *
     * @return Spring handler.
     */
    public Object getSpringHandler() {
        return this.handler;
    }

    /**
     * Obtains the {@link RequestMappingHandlerAdapter}.
     *
     * @return {@link RequestMappingHandlerAdapter}.
     */
    public RequestMappingHandlerAdapter getRequestMappingHandlerAdapter() {
        return this.handlerAdapter;
    }

    /**
     * Obtains the {@link DispatcherServlet}.
     *
     * @return {@link DispatcherServlet}.
     */
    public DispatcherServlet getDispatcherServlet() {
        return this.dispatcherServlet;
    }

    /**
     * Obtains the {@link ApplicationContext}.
     *
     * @return {@link ApplicationContext}.
     */
    public ApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * Obtains the {@link ModelAndViewContainer}.
     *
     * @return {@link ModelAndViewContainer}.
     */
    public ModelAndViewContainer getModelAndViewContainer() {
        return this.mavContainer;
    }

    /**
     * Obtains the {@link NativeWebRequest}.
     *
     * @return {@link NativeWebRequest}.
     */
    public NativeWebRequest getNativeWebRequest() {
        return this.webRequest;
    }

    /**
     * Obtains the {@link ModelAndViewBridge} for the {@link Method}.
     *
     * @param method {@link Method}.
     * @return {@link ModelAndViewBridge} for the {@link Method}.
     * @throws Exception If fails to construct {@link ModelAndViewBridge} to the {@link Method}.
     */
    public ModelAndViewBridge getModelAndViewBridge(Method method) throws Exception {

        // Lazy obtain the bridge
        ModelAndViewBridge bridge = this.modelAndViewBridges.get(method);
        if (bridge == null) {
            bridge = new ModelAndViewBridge(method, this.getModelAndViewContainer(), this.getSpringHandler(),
                    this.getRequestMappingHandlerAdapter(), this.getHttpServletRequest(), this.getHttpServletResponse(),
                    this.getNativeWebRequest(), this.getDispatcherServlet(), this.getApplicationContext());
            this.modelAndViewBridges.put(method, bridge);
        }

        // Flag the last model and view
        this.lastModelAndView = bridge;
        return bridge;
    }

    /**
     * Obtains the {@link ModelAndViewBridge} to render result.
     *
     * @return {@link ModelAndViewBridge} to render result.
     * @throws Exception If fails to obtain.
     */
    public ModelAndViewBridge getRenderModelAndViewBridge() throws Exception {
        if (this.lastModelAndView == null) {
            return this.getModelAndViewBridge(Object.class.getMethod("hashCode"));
        } else {
            return this.lastModelAndView;
        }
    }

}
