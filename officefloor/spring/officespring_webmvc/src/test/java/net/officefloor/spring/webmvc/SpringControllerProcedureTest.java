package net.officefloor.spring.webmvc;

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
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.tutorial.springapp.Application;

/**
 * Ensure can invoke Spring {@link Controller}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringControllerProcedureTest extends OfficeFrameTestCase {

	public void test() throws Exception {

		// Configure
		List<HandlerMapping> handlerMappings = new ArrayList<>();
		ConfigurableApplicationContext context = new SpringApplicationBuilder(Application.class).run();
		Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context,
				HandlerMapping.class, true, false);
		if (!matchingBeans.isEmpty()) {
			handlerMappings = new ArrayList<>(matchingBeans.values());
			AnnotationAwareOrderComparator.sort(handlerMappings);
		}

		// Obtain the controllers
		Map<String, Object> controllers = context.getBeansWithAnnotation(Controller.class);
		for (String controllerName : controllers.keySet()) {
			Object controller = controllers.get(controllerName);

			// Find controller methods
			NEXT_METHOD: for (Method method : controller.getClass().getMethods()) {

				// Obtain the mapping annotation
				RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method,
						RequestMapping.class);
				if (requestMapping == null) {
					continue NEXT_METHOD;
				}

				// Extract information for controller
				System.out.println();
				System.out.println("CONTROLLER: " + controller + " method: " + method.getName());

				// Obtain details
				String httpMethod = requestMapping.method().length > 0 ? requestMapping.method()[0].name() : "GET";
				String path = requestMapping.path().length > 0 ? requestMapping.path()[0] : "";
				String consumes = requestMapping.consumes().length > 0 ? requestMapping.consumes()[0] : null;

				// Create request to find handler execution chain
				Map<String, String> headers = new HashMap<>();
				for (String headerName : requestMapping.headers()) {
					headers.put(headerName, "VALUE");
				}
				if (consumes != null) {
					headers.put(HttpHeaders.ACCEPT, consumes);
				}
				Map<String, Object> attributes = new HashMap<>();
				HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
						this.getClass().getClassLoader(), new Class[] { HttpServletRequest.class },
						(proxy, proxyMethod, args) -> {
							switch (proxyMethod.getName()) {

							case "getMethod":
								return httpMethod;

							case "getRequestURI":
							case "getServletPath":
								return path;

							case "getContextPath":
								return "";

							case "getHeader":
								return headers.get(args[0]);
							case "getHeaders":
								String headerValue = headers.get(args[0]);
								return new Vector<>(
										headerValue != null ? Arrays.asList(headerValue) : Collections.emptyList())
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
								throw new IllegalStateException("Proxy method " + method.getName());
							}
						});

				// Find mapping
				for (HandlerMapping mapping : handlerMappings) {

					// Obtain the handler
					System.out.println("Mapping " + mapping);
					HandlerExecutionChain chain = mapping.getHandler(request);
					if (chain != null) {
						System.out.println("\tchain: " + chain);
						continue NEXT_METHOD;
					}
				}
			}
		}
	}

}