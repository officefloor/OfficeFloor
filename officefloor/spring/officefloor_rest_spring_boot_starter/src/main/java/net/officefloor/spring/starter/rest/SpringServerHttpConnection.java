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
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.function.Supplier;

public class SpringServerHttpConnection extends ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> {

    private static final Method processDispatchResult;

    static {
        final String METHOD_NAME = "processDispatchResult";
        try {
            processDispatchResult = DispatcherServlet.class.getDeclaredMethod(METHOD_NAME,
                    HttpServletRequest.class, HttpServletResponse.class, HandlerExecutionChain.class,
                    ModelAndView.class, Exception.class);
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Unable to obtain " + DispatcherServlet.class.getName()
                    + "." + METHOD_NAME + "(...)", ex);
        }
        processDispatchResult.setAccessible(true);
    }

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final Object handler;

    private final RequestMappingHandlerAdapter handlerAdapter;

    private final DispatcherServlet dispatcherServlet;

    private final ApplicationContext applicationContext;

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
     * Delegates to the {@link DispatcherServlet} to process the results.
     *
     * @param modelAndView {@link ModelAndView}. May be <code>null</code>.
     * @param ex           {@link Exception}. May be <code>null</code>.
     * @throws Exception If fails to process.
     */
    public void processDispatchResult(ModelAndView modelAndView, Throwable ex) throws Exception {

        // Transform to exception
        Exception exception;
        if (ex instanceof Exception) {
            exception = (Exception) ex;
        } else {
            exception = new ServletException(ex);
        }

        // Create the handler chain
        HandlerExecutionChain chain = new HandlerExecutionChain(this.getSpringHandler());

        // Obtain the dispatcher servlet
        DispatcherServlet servlet = this.getDispatcherServlet();

        // If within test, use TestDispatcherServlet (reflection to avoid imports)
        Object mockMvc = this.applicationContext.getBean("mockMvc");
        if (mockMvc != null) {
            Method getDispatchServletMethod = mockMvc.getClass().getDeclaredMethod("getDispatcherServlet");
            servlet = (DispatcherServlet) getDispatchServletMethod.invoke(mockMvc);
        }

        // Delegate to Dispatcher Servlet
        processDispatchResult.invoke(servlet, this.getHttpServletRequest(),
                this.getHttpServletResponse(), chain, modelAndView, exception);
    }
}
