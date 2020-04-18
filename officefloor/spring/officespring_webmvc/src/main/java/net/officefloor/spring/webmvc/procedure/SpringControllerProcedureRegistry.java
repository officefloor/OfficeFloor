package net.officefloor.spring.webmvc.procedure;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.AfterSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;

/**
 * Registry to map {@link SpringControllerProcedureSource} to its
 * {@link Controller} {@link HandlerExecutionChain}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringControllerProcedureRegistry
		implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

	/**
	 * {@link SpringControllerProcedureRegistry}.
	 */
	private static final ThreadLocal<SpringControllerProcedureRegistry> registry = new ThreadLocal<>();

	/**
	 * Registers a {@link SpringControllerProcedure}.
	 * 
	 * @param procedure {@link SpringControllerProcedure}.
	 */
	public static void registerSpringControllerProcedure(SpringControllerProcedure procedure) {
		registry.get().registeredProcedures.add(procedure);
	}

	/**
	 * Obtains the {@link HandlerExecutionChain} for the {@link Controller}
	 * {@link Method}.
	 * 
	 * @param controllerClass      {@link Controller} {@link Class}.
	 * @param controllerMethodName Name of the {@link Controller} {@link Method}.
	 * @param classLoader          {@link ClassLoader}.
	 * @param springContext        {@link ConfigurableApplicationContext}.
	 * @return {@link HandlerExecutionChain} for the {@link Controller}
	 *         {@link Method}. <code>null</code> if not found.
	 * @throws Exception If fails to obtain the {@link HandlerExecutionChain}.
	 */
	public static HandlerExecutionChain getHandler(Class<?> controllerClass, String controllerMethodName,
			ClassLoader classLoader, ConfigurableApplicationContext springContext) throws Exception {

		// Find method on controller
		for (Method method : controllerClass.getMethods()) {
			if (controllerMethodName.equals(method.getName())) {

				// Method found, so return handler
				return getHandler(controllerClass, method, classLoader, springContext);
			}
		}

		// As here, method not found so no handler
		return null;
	}

	/**
	 * Obtains the {@link HandlerExecutionChain} for the {@link Controller}
	 * {@link Method}.
	 * 
	 * @param controllerClass  {@link Controller} {@link Class}.
	 * @param controllerMethod {@link Controller} {@link Method}.
	 * @param classLoader      {@link ClassLoader}.
	 * @param springContext    {@link ConfigurableApplicationContext}.
	 * @return {@link HandlerExecutionChain} for the {@link Controller}
	 *         {@link Method}. <code>null</code> if not found.
	 * @throws Exception If fails to obtain the {@link HandlerExecutionChain}.
	 */
	public static HandlerExecutionChain getHandler(Class<?> controllerClass, Method controllerMethod,
			ClassLoader classLoader, ConfigurableApplicationContext springContext) throws Exception {

		// Obtain the class request mapping
		RequestMapping classRequestMapping = AnnotatedElementUtils.findMergedAnnotation(controllerClass,
				RequestMapping.class);
		String pathPrefix = (classRequestMapping != null) && (classRequestMapping.path().length > 0)
				? classRequestMapping.path()[0]
				: "";

		// Obtain the mapping annotation
		RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(controllerMethod,
				RequestMapping.class);
		if (requestMapping == null) {
			return null; // method not controller method
		}

		// Obtain details
		String httpMethod = requestMapping.method().length > 0 ? requestMapping.method()[0].name() : "GET";
		String path = pathPrefix + (requestMapping.path().length > 0 ? requestMapping.path()[0] : "");
		String consumes = requestMapping.consumes().length > 0 ? requestMapping.consumes()[0] : null;

		// Create the parameters
		Map<String, String[]> parameterMap = new HashMap<>();
		for (String parameterName : requestMapping.params()) {
			parameterMap.put(parameterName, new String[] { "VALUE" });
		}

		// Create the headers
		Map<String, String> headers = new HashMap<>();
		for (String headerName : requestMapping.headers()) {
			headers.put(headerName, "VALUE");
		}
		if (consumes != null) {
			headers.put(HttpHeaders.ACCEPT, consumes);
		}

		// Create request to find handler execution chain
		Map<String, Object> attributes = new HashMap<>();
		HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(classLoader,
				new Class[] { HttpServletRequest.class }, (proxy, proxyMethod, args) -> {
					switch (proxyMethod.getName()) {

					case "getMethod":
						return httpMethod;

					case "getContextPath":
						return "";

					case "getRequestURI":
					case "getServletPath":
						return path;

					case "getParameterMap":
						return parameterMap;

					case "getHeader":
						return headers.get(args[0]);
					case "getHeaders":
						String headerValue = headers.get(args[0]);
						return new Vector<>(headerValue != null ? Arrays.asList(headerValue) : Collections.emptyList())
								.elements();

					case "getAttribute":
						return attributes.get(args[0]);
					case "removeAttribute":
						return attributes.remove(args[0]);
					case "setAttribute":
						attributes.put((String) args[0], args[1]);
						return null;

					case "getCharacterEncoding":
						return null;

					default:
						throw new IllegalStateException(
								"Proxy method " + controllerClass.getName() + "#" + controllerMethod.getName());
					}
				});

		// Obtain the handler mappings
		List<HandlerMapping> handlerMappings = new ArrayList<>();
		Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(springContext,
				HandlerMapping.class, true, false);
		if (!matchingBeans.isEmpty()) {
			handlerMappings = new ArrayList<>(matchingBeans.values());
			AnnotationAwareOrderComparator.sort(handlerMappings);
		}

		// Find mapping
		for (HandlerMapping mapping : handlerMappings) {

			// Obtain the handler
			HandlerExecutionChain chain = mapping.getHandler(request);
			if (chain != null) {
				return chain;
			}
		}

		// Did not find handler
		return null;
	}

	/**
	 * Registered {@link SpringControllerProcedure} instances.
	 */
	private List<SpringControllerProcedure> registeredProcedures = new ArrayList<>();

	/**
	 * {@link ClassLoader}.
	 */
	private ClassLoader classLoader;

	/*
	 * =================== SpringSupplierExtensionServiceFactory ===================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {

		// Configure
		this.classLoader = context.getClassLoader();

		// Register
		registry.set(this);

		// Use extension
		return this;
	}

	/*
	 * ========================== SpringSupplierExtension ==========================
	 */

	@Override
	public void afterSpringLoad(AfterSpringLoadSupplierExtensionContext context) throws Exception {

		// Obtain spring context
		ConfigurableApplicationContext springContext = context.getSpringContext();

		// Load the attributes for each procedure
		for (SpringControllerProcedure procedure : this.registeredProcedures) {

			// Obtain the handler
			HandlerExecutionChain handler = getHandler(procedure.controllerClass, procedure.controllerMethodName,
					this.classLoader, springContext);
			if (handler == null) {
				throw new IllegalStateException(
						"No " + HandlerExecutionChain.class.getSimpleName() + " for " + Controller.class.getSimpleName()
								+ " " + procedure.controllerClass.getName() + "#" + procedure.controllerMethodName);
			}

			// Load handler in attributes for procedure
			Map<String, Object> attributes = new HashMap<>();
			attributes.put(ProcedureDispatcherServlet.ATTRIBUTE_HANDLER_EXECUTION_CHAIN, handler);
			procedure.attributes = attributes;
		}

		// Stop registration
		registry.remove();
	}

}