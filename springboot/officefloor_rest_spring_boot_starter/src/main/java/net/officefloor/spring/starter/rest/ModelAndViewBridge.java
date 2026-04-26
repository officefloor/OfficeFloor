package net.officefloor.spring.starter.rest;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ModelFactory;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Bridge to Spring for the {@link ModelAndView}.
 */
public class ModelAndViewBridge {

    private static final Method getDataBinderFactoryMethod;

    private static final Method getModelFactoryMethod;

    private static final Method getModelAndViewMethod;

    private static final Method processDispatchResultMethod;

    private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName, parameterTypes);
        } catch (Exception ex) {
            throw new IllegalStateException("Must be able to find method " + clazz.getName() + "." + methodName + "(...)", ex);
        }
        method.setAccessible(true);
        return method;
    }

    static {
        getDataBinderFactoryMethod = getMethod(RequestMappingHandlerAdapter.class, "getDataBinderFactory", HandlerMethod.class);
        getModelFactoryMethod = getMethod(RequestMappingHandlerAdapter.class, "getModelFactory", HandlerMethod.class, WebDataBinderFactory.class);
        getModelAndViewMethod = getMethod(RequestMappingHandlerAdapter.class, "getModelAndView", ModelAndViewContainer.class, ModelFactory.class, NativeWebRequest.class);
        processDispatchResultMethod = getMethod(DispatcherServlet.class, "processDispatchResult", HttpServletRequest.class,
                HttpServletResponse.class, HandlerExecutionChain.class, ModelAndView.class, Exception.class);
    }

    private final ModelAndViewContainer modelAndViewContainer;

    private final Object handler;

    private final RequestMappingHandlerAdapter handlerAdapter;

    private final HttpServletRequest servletRequest;

    private final HttpServletResponse servletResponse;

    private final NativeWebRequest webRequest;

    private final DispatcherServlet dispatcherServlet;

    private final ApplicationContext applicationContext;

    private final HandlerMethod handlerMethod;

    private final WebDataBinderFactory webDataBinderFactory;

    private final ModelFactory modelFactory;

    public ModelAndViewBridge(Method method, ModelAndViewContainer modelAndViewContainer, Object handler,
                              RequestMappingHandlerAdapter handlerAdapter, HttpServletRequest servletRequest,
                              HttpServletResponse servletResponse, NativeWebRequest webRequest,
                              DispatcherServlet dispatcherServlet, ApplicationContext applicationContext) throws Exception {
        this.modelAndViewContainer = modelAndViewContainer;
        this.handler = handler;
        this.handlerAdapter = handlerAdapter;
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        this.webRequest = webRequest;
        this.dispatcherServlet = dispatcherServlet;
        this.applicationContext = applicationContext;

        // Extract out the state
        this.handlerMethod = new OfficeFloorHandlerMethod(method);
        this.webDataBinderFactory = (WebDataBinderFactory) getDataBinderFactoryMethod.invoke(this.handlerAdapter, this.handlerMethod);
        this.modelFactory = (ModelFactory) getModelFactoryMethod.invoke(this.handlerAdapter, this.handlerMethod, this.webDataBinderFactory);
        this.modelFactory.initModel(webRequest, this.modelAndViewContainer, this.handlerMethod);
    }

    /**
     * Obtains the {@link ModelAndViewContainer}.
     *
     * @return {@link ModelAndViewContainer}.
     */
    public ModelAndViewContainer getModelAndViewContainer() {
        return this.modelAndViewContainer;
    }

    /**
     * Obtains the {@link HandlerMethod}.
     *
     * @return {@link HandlerMethod}.
     */
    public HandlerMethod getHandlerMethod() {
        return this.handlerMethod;
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
     * Obtains the {@link WebDataBinderFactory}.
     *
     * @return {@link WebDataBinderFactory}.
     */
    public WebDataBinderFactory getWebDataBinderFactory() throws Exception {
        return this.webDataBinderFactory;
    }

    /**
     * Obtains the {@link ModelFactory}.
     *
     * @return {@link ModelFactory}.
     */
    public ModelFactory getModelFactory() throws Exception {
        return this.modelFactory;
    }

    /**
     * Obtains the {@link HttpServletRequest}.
     *
     * @return {@link HttpServletRequest}.
     */
    public HttpServletRequest getHttpServletRequest() {
        return this.servletRequest;
    }

    /**
     * Obtains the {@link HttpServletResponse}.
     *
     * @return {@link HttpServletResponse}.
     */
    public HttpServletResponse getHttpServletResponse() {
        return this.servletResponse;
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
     * Writes the return value.
     *
     * @param returnValue Return value.
     * @throws Exception If fails to write the return value.
     */
    public void writeObjectResponse(Object returnValue) throws Exception {

        // Create the Method Parameter
        MethodParameter methodParameter = null;

        // Obtain the native request
        NativeWebRequest nativeWebRequest = this.getNativeWebRequest();

        // Create handlers
        List<HandlerMethodReturnValueHandler> handlers = this.handlerAdapter.getReturnValueHandlers();
        for (HandlerMethodReturnValueHandler handler : handlers) {
            if (handler.supportsReturnType(methodParameter)) {
                handler.handleReturnValue(returnValue, methodParameter, null, nativeWebRequest);
                return; // handled
            }
        }
    }

    /**
     * Obtains the {@link ModelAndView} from the current state.
     *
     * @param view Name of the view.
     * @return {@link ModelAndView} from the current state.
     */
    public ModelAndView getModelAndView(String view) throws Exception {
        this.modelAndViewContainer.setViewName(view);
        if (view.startsWith("redirect:")) {
            this.modelAndViewContainer.setRedirectModelScenario(true);
        }
        return (ModelAndView) getModelAndViewMethod.invoke(this.getRequestMappingHandlerAdapter(),
                this.getModelAndViewContainer(), this.getModelFactory(), this.getNativeWebRequest());
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
        if (ex != null) {
            // Obtain the exception for processing
            if (ex instanceof Exception) {
                exception = (Exception) ex;
            } else {
                exception = new ServletException(ex);
            }
        } else {
            // No exception
            exception = null;
        }

        // Create the handler chain
        HandlerExecutionChain chain = new HandlerExecutionChain(this.handler);

        // Obtain the dispatcher servlet
        DispatcherServlet servlet = this.getDispatcherServlet();

        // If within test, use TestDispatcherServlet (reflection to avoid imports)
        Object mockMvc = this.getApplicationContext().getBean("mockMvc");
        if (mockMvc != null) {
            Method getDispatchServletMethod = mockMvc.getClass().getDeclaredMethod("getDispatcherServlet");
            servlet = (DispatcherServlet) getDispatchServletMethod.invoke(mockMvc);
        }

        // Delegate to Dispatcher Servlet
        processDispatchResultMethod.invoke(servlet, this.getHttpServletRequest(),
                this.getHttpServletResponse(), chain, modelAndView, exception);
    }


    public class OfficeFloorHandlerMethod extends HandlerMethod {

        private OfficeFloorHandlerMethod(Method method) {
            super(new Object(), method);
        }

        @Override
        public Class<?> getBeanType() {
            return this.getMethod().getDeclaringClass();
        }
    }

}
