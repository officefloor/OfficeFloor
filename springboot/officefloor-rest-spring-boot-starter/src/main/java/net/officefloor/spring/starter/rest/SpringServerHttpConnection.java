/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring.starter.rest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.plugin.section.clazz.MethodParameterAnnotation;
import net.officefloor.server.http.DateHttpHeaderClock;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpResponseWriter;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.stream.StreamBufferPool;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import net.officefloor.spring.starter.rest.response.SpringHttpObjectResponderFactory;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * {@link ProcessAwareServerHttpConnectionManagedObject} for Spring MVC.
 */
public class SpringServerHttpConnection extends ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> {

    /**
     * {@link Method} to use if no {@link MethodParameterAnnotation}.
     *
     * @param response {@link ObjectResponse}.
     */
    public static void methodParameter(ObjectResponse<Object> response) {
    }

    private static final Method METHOD_PARAMETER;

    static {
        final String methodName = "methodParameter";
        try {
            METHOD_PARAMETER = SpringServerHttpConnection.class.getMethod(methodName, ObjectResponse.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Should find method " + methodName);
        }
    }

    private final HttpServletRequest request;

    private final HttpServletResponse response;

    private final Object handler;

    private final RequestMappingHandlerAdapter handlerAdapter;

    private final DispatcherServlet dispatcherServlet;

    private final ApplicationContext applicationContext;

    private final ModelAndViewContainer mavContainer;

    private final NativeWebRequest webRequest;

    private final Map<Method, ModelAndViewBridge> modelAndViewBridges = new HashMap<>();

    private final List<SpringExceptionHandler> exceptionHandlers;

    private ModelAndViewBridge lastModelAndView = null;

    private Object responseObject = null;

    private ManagedFunctionObjectType<?> managedFunctionObjectType = null;

    private String responseView = null;

    private Throwable responseFailure = null;

    private boolean isManuallyHandle = false;

    /**
     * Instantiate.
     *
     * @param serverLocation                  {@link HttpServerLocation}.
     * @param isSecure                        Indicates if secure.
     * @param methodSupplier                  {@link Supplier} for the
     *                                        <code>HttpRequest</code>
     *                                        {@link HttpMethod}.
     * @param requestUriSupplier              {@link Supplier} for the
     *                                        <code>HttpRequest</code> URI.
     * @param version                         {@link HttpVersion} for the
     *                                        <code>HttpRequest</code>.
     * @param requestHeaders                  {@link NonMaterialisedHttpHeaders} for
     *                                        the <code>HttpRequest</code>.
     * @param requestEntity                   {@link ByteSequence} for the
     *                                        <code>HttpRequest</code> entity.
     * @param serverName                      Name of the server. May be
     *                                        <code>null</code> if not sending
     *                                        <code>Server</code>
     *                                        <code>HttpHeader</code>.
     * @param dateHttpHeaderClock             {@link DateHttpHeaderClock}. May be
     *                                        <code>null</code> to not send
     *                                        <code>Date</code> <code>HttpHeader</code>.
     * @param isIncludeStackTraceOnEscalation <code>true</code> to include the
     *                                        <code>Escalation</code> stack trace in the
     *                                        <code>HttpResponse</code>.
     * @param writer                          {@link HttpResponseWriter}.
     * @param bufferPool                      {@link StreamBufferPool}.
     * @param request                         {@link HttpServletRequest}.
     * @param response                        {@link HttpServletResponse}.
     * @param handler                         Handler object.
     * @param handlerAdapter                  {@link RequestMappingHandlerAdapter}.
     * @param dispatcherServlet               {@link DispatcherServlet}.
     * @param applicationContext              {@link ApplicationContext}.
     * @param exceptionHandlers               {@link SpringExceptionHandler} instances.
     */
    public SpringServerHttpConnection(HttpServerLocation serverLocation,
                                      boolean isSecure, Supplier<HttpMethod> methodSupplier, Supplier<String> requestUriSupplier,
                                      HttpVersion version, NonMaterialisedHttpHeaders requestHeaders, ByteSequence requestEntity,
                                      HttpHeaderValue serverName, DateHttpHeaderClock dateHttpHeaderClock, boolean isIncludeStackTraceOnEscalation,
                                      HttpResponseWriter<ByteBuffer> writer, StreamBufferPool<ByteBuffer> bufferPool,
                                      HttpServletRequest request, HttpServletResponse response, Object handler,
                                      RequestMappingHandlerAdapter handlerAdapter, DispatcherServlet dispatcherServlet, ApplicationContext applicationContext,
                                      List<SpringExceptionHandler> exceptionHandlers) {
        super(serverLocation, isSecure, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity, serverName, dateHttpHeaderClock, isIncludeStackTraceOnEscalation, writer, bufferPool);
        this.request = request;
        this.response = response;
        this.handler = handler;
        this.handlerAdapter = handlerAdapter;
        this.dispatcherServlet = dispatcherServlet;
        this.applicationContext = applicationContext;
        this.exceptionHandlers = exceptionHandlers;

        // Create the additional state
        this.mavContainer = new ModelAndViewContainer();
        this.webRequest = new ServletWebRequest(request, response);
    }

    /**
     * Specifies the response.
     *
     * @param responseObject            Response object.
     * @param managedFunctionObjectType {@link ManagedFunctionObjectType}.
     * @param responseView              Response view.
     * @param responseFailure           Response {@link Throwable}.
     */
    public void setResponse(Object responseObject, ManagedFunctionObjectType<?> managedFunctionObjectType, String responseView, Throwable responseFailure) {
        this.responseObject = responseObject;
        this.managedFunctionObjectType = managedFunctionObjectType;
        this.responseView = responseView;
        this.responseFailure = responseFailure;
    }

    /**
     * Flags manually handling {@link HttpServletResponse}.
     */
    public void flagManuallyHandle() {
        this.isManuallyHandle = true;
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

    /**
     * Write the response.
     *
     * @throws Throwable If fails to write response.
     */
    public void writeResponse() throws Throwable {

        try {

            // Determine if failure
            if (this.responseFailure != null) {
                throw this.responseFailure;
            }

            // Determine if response object
            if (this.responseObject != null) {

                // Obtain the native web request
                NativeWebRequest nativeWebRequest = this.getNativeWebRequest();

                // Obtain the return value handlers
                RequestMappingHandlerAdapter handlerAdapter = this.getRequestMappingHandlerAdapter();
                List<HandlerMethodReturnValueHandler> returnValueHandlers = handlerAdapter.getReturnValueHandlers();

                // Determine the method parameter
                Method method;
                MethodParameterAnnotation annotation = this.managedFunctionObjectType.getAnnotation(MethodParameterAnnotation.class);
                if (annotation != null) {
                    // Have annotation, so use
                    method = annotation.getMethod();
                } else {
                    // Use default method
                    method = METHOD_PARAMETER;
                }
                ObjectResponseAnnotatedMethod objectResponseMethod = new ObjectResponseAnnotatedMethod(method);
                MethodParameter methodParameter = objectResponseMethod.getReturnType(this.responseObject);

                // Determine if ResponseStatus
                ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);
                if (responseStatus != null) {
                    this.getHttpServletResponse().setStatus(responseStatus.value().value());
                }

                // Determine if annotated HTTP status (overrides less specific @ResponseStatus)
                HttpResponse httpResponse = this.managedFunctionObjectType.getAnnotation(HttpResponse.class);
                if (httpResponse != null) {
                    this.getHttpServletResponse().setStatus(httpResponse.status());
                }

                // Obtain the model and view container
                ModelAndViewBridge mavBridge = this.getModelAndViewBridge(method);
                ModelAndViewContainer mavContainer = mavBridge.getModelAndViewContainer();

                // Handle response
                for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
                    if (returnValueHandler.supportsReturnType(methodParameter)) {

                        // Handle the return value
                        returnValueHandler.handleReturnValue(this.responseObject, methodParameter, mavContainer, nativeWebRequest);

                        // Determine if view to render
                        String view = mavContainer.getViewName();
                        if (view != null) {
                            ModelAndView mav = mavBridge.getModelAndView(view);
                            mavBridge.processDispatchResult(mav, null);
                        }

                        // Handled
                        return;
                    }
                }
            }

            // Determine if response view
            if (this.responseView != null) {

                // Obtain the bridge to render result
                ModelAndViewBridge renderBridge = this.getRenderModelAndViewBridge();

                // Send the response
                ModelAndView modelAndView = renderBridge.getModelAndView(this.responseView);
                renderBridge.processDispatchResult(modelAndView, null);

                // Handled
                return;
            }

            // Determine if not manually handling response
            if (!this.isManuallyHandle) {
                // As here, no content for response
                this.response.setStatus(204);
            }

        } catch (Exception ex) {
            try {
                ModelAndViewBridge bridge = this.getRenderModelAndViewBridge();
                bridge.processDispatchResult(null, ex);

            } catch (Exception writeEx) {

                // Handle invocation target failure (as reflectively invoked)
                Throwable targetEx = writeEx;
                if (writeEx instanceof InvocationTargetException) {
                    targetEx = ((InvocationTargetException) writeEx).getTargetException();
                }

                // Attempt to handle the exception
                HANDLED:
                for (SpringExceptionHandler exceptionHandler : this.exceptionHandlers) {
                    try {
                        if (exceptionHandler.handle(targetEx, this)) {
                            // Handled
                            targetEx = null;
                            break HANDLED;
                        }
                    } catch (Throwable handlingFailure) {
                        // Process as if were a filter chain propagating up exception
                        targetEx = handlingFailure;
                    }
                }
                if (targetEx != null) {
                    // Exception not handled, so propagate
                    throw targetEx;
                }
            }
        }
    }

    private static class ObjectResponseAnnotatedMethod extends AnnotatedMethod {

        public ObjectResponseAnnotatedMethod(Method method) {
            super(method);
        }

        public MethodParameter getReturnType(Object returnValue) {
            return new ObjectResponseReturnValueMethodParameter(returnValue);
        }

        @RestController
        private class ObjectResponseReturnValueMethodParameter extends AnnotatedMethodParameter {

            private final Class<?> returnValueType;

            public ObjectResponseReturnValueMethodParameter(Object returnValue) {
                super(-1);
                this.returnValueType = (returnValue != null ? returnValue.getClass() : null);
            }

            @Override
            public Class<?> getParameterType() {
                return (this.returnValueType != null ? this.returnValueType : super.getParameterType());
            }

            @Override
            public Class<?> getContainingClass() {
                return this.getClass();
            }
        }
    }

}
