package net.officefloor.spring.starter.rest.response;

import net.officefloor.plugin.section.clazz.MethodParameterAnnotation;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpExternalResponse;
import net.officefloor.spring.starter.rest.ModelAndViewBridge;
import net.officefloor.spring.starter.rest.SpringServerHttpConnection;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import net.officefloor.web.build.HttpEscalationResponder;
import net.officefloor.web.build.HttpEscalationResponderContext;
import net.officefloor.web.build.HttpObjectResponder;
import net.officefloor.web.build.HttpObjectResponderContext;
import net.officefloor.web.build.HttpObjectResponderFactory;
import org.apache.coyote.Response;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotatedMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class SpringHttpObjectResponderFactory implements HttpObjectResponderFactory,
        HttpObjectResponder<Object>, HttpEscalationResponder<Throwable> {

    /**
     * {@link Method} to use if no {@link MethodParameterAnnotation}.
     */
    public static void methodParameter(ObjectResponse<Object> response) {
    }

    private static final Method METHOD_PARAMETER;

    static {
        final String methodName = "methodParameter";
        try {
            METHOD_PARAMETER = SpringHttpObjectResponderFactory.class.getMethod(methodName, ObjectResponse.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Should find method " + methodName);
        }
    }

    /**
     * {@link SpringExceptionHandler} instances.
     */
    private final SpringExceptionHandler[] exceptionHandlers;

    /**
     * Initiate.
     *
     * @param exceptionHandlers {@link SpringExceptionHandler} instances.
     */
    public SpringHttpObjectResponderFactory(SpringExceptionHandler[] exceptionHandlers) {
        this.exceptionHandlers = exceptionHandlers;
    }

    /*
     * ================== HttpObjectResponderFactory ===============
     */

    @Override
    public String getContentType() {
        return "*/*";
    }

    @Override
    public <T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType) {
        return (HttpObjectResponder<T>) this;
    }

    @Override
    public <E extends Throwable> HttpEscalationResponder<E> createHttpEscalationResponder(Class<E> escalationType, boolean isOfficeFloorEscalation) {
        return isOfficeFloorEscalation ? null : (HttpEscalationResponder<E>) this;
    }

    /*
     * ===================== HttpObjectResponder ====================
     */

    @Override
    public void send(HttpObjectResponderContext<Object> context) throws IOException {
        try {
            // Obtain the Spring details
            SpringServerHttpConnection connection = (SpringServerHttpConnection) context.getServerHttpConnection();
            NativeWebRequest nativeWebRequest = connection.getNativeWebRequest();

            // Obtain the return value handlers
            RequestMappingHandlerAdapter handlerAdapter = connection.getRequestMappingHandlerAdapter();
            List<HandlerMethodReturnValueHandler> returnValueHandlers = handlerAdapter.getReturnValueHandlers();

            // Obtain the response object (return value)
            Object returnValue = context.getResponseObject();

            // Determine the method parameter
            Method method;
            MethodParameterAnnotation annotation = context.getManagedFunctionObjectType().getAnnotation(MethodParameterAnnotation.class);
            if (annotation != null) {
                // Have annotation, so use
                method = annotation.getMethod();
            } else {
                // Use default method
                method = METHOD_PARAMETER;
            }
            ObjectResponseAnnotatedMethod objectResponseMethod = new ObjectResponseAnnotatedMethod(method);
            MethodParameter methodParameter = objectResponseMethod.getReturnType(returnValue);

            // Determine if ResponseStatus
            ResponseStatus responseStatus = method.getAnnotation(ResponseStatus.class);
            if (responseStatus != null) {
                connection.getHttpServletResponse().setStatus(responseStatus.value().value());
            }

            // Determine if annotated HTTP status (overrides less specific @ResponseStatus)
            HttpResponse httpResponse = context.getManagedFunctionObjectType().getAnnotation(HttpResponse.class);
            if (httpResponse != null) {
                connection.getHttpServletResponse().setStatus(httpResponse.status());
            }

            // Obtain the model and view container
            ModelAndViewBridge mavBridge = connection.getModelAndViewBridge(method);
            ModelAndViewContainer mavContainer = mavBridge.getModelAndViewContainer();

            // Handle response
            for (HandlerMethodReturnValueHandler returnValueHandler : returnValueHandlers) {
                if (returnValueHandler.supportsReturnType(methodParameter)) {

                    // Handle the return value
                    returnValueHandler.handleReturnValue(returnValue, methodParameter, mavContainer, nativeWebRequest);

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

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new HttpException(ex);
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

    /*
     * ====================== HttpObjectResponder ========================
     */

    @Override
    public void send(HttpEscalationResponderContext<Throwable> context) throws IOException {

        // Delegate to Spring to handle
        SpringServerHttpConnection springConnection = (SpringServerHttpConnection) context.getServerHttpConnection();
        try {
            try {
                ModelAndViewBridge bridge = springConnection.getRenderModelAndViewBridge();
                bridge.processDispatchResult(null, context.getEscalation());
            } catch (Exception ex) {

                // Handle invocation target failure (as reflectively invoked)
                Throwable targetEx = ex;
                if (ex instanceof InvocationTargetException) {
                    targetEx = ((InvocationTargetException) ex).getTargetException();
                }

                // Attempt to handle the exception
                HANDLED: for (SpringExceptionHandler exceptionHandler : this.exceptionHandlers) {
                    try {
                        if (exceptionHandler.handle(targetEx, springConnection)) {
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

        } catch (Throwable ex) {
            // Propagate failure
            if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            } else if (ex instanceof Error) {
                throw (Error) ex;
            } else {
                throw new IOException(ex);
            }
        }

        // Flag that externally handled (by Spring)
        HttpExternalResponse.of(springConnection.getResponse()).externalSend();
    }

}
