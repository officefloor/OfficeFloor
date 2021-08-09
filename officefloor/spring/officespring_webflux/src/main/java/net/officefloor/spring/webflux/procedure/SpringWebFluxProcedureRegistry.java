/*-
 * #%L
 * Spring Web Flux Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.spring.webflux.procedure;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.DispatcherHandler;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.adapter.HttpWebHandlerAdapter;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.spring.extension.AfterSpringLoadSupplierExtensionContext;
import net.officefloor.spring.extension.SpringSupplierExtension;
import net.officefloor.spring.extension.SpringSupplierExtensionServiceFactory;
import net.officefloor.spring.webflux.OfficeFloorServerHttpRequest;
import reactor.core.publisher.Mono;

/**
 * Registry to map {@link SpringWebFluxProcedureSource} to its
 * {@link Controller} {@link HttpHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedureRegistry implements SpringSupplierExtensionServiceFactory, SpringSupplierExtension {

	/**
	 * {@link SpringWebFluxProcedureRegistry}.
	 */
	private static final ThreadLocal<SpringWebFluxProcedureRegistry> registry = new ThreadLocal<>();

	/**
	 * Registers a {@link SpringWebFluxProcedure}.
	 * 
	 * @param procedure {@link SpringWebFluxProcedure}.
	 */
	public static void registerSpringControllerProcedure(SpringWebFluxProcedure procedure) {
		registry.get().registeredProcedures.add(procedure);
	}

	/**
	 * Extract the {@link RequestMapping} {@link Method} instances.
	 * 
	 * @param controllerClass {@link Class} that may be {@link Controller}.
	 * @param methodVisitor   Receives extract {@link RequestMapping} {@link Method}
	 *                        instances.
	 */
	public static void extractEndPointMethods(Class<?> controllerClass, Consumer<Method> methodVisitor) {

		// Ensure controller class
		Controller controller = AnnotatedElementUtils.findMergedAnnotation(controllerClass, Controller.class);
		if (controller == null) {
			return; // not controller
		}

		// Load all the request mapped methods
		NEXT_METHOD: for (Method method : controllerClass.getMethods()) {

			// Determine if request mapped method
			RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
			if (requestMapping == null) {
				continue NEXT_METHOD;
			}

			// Include the request mapped method
			methodVisitor.accept(method);
		}
	}

	/**
	 * Registered {@link SpringWebFluxProcedure} instances.
	 */
	private List<SpringWebFluxProcedure> registeredProcedures = new ArrayList<>();

	/*
	 * =================== SpringSupplierExtensionServiceFactory ===================
	 */

	@Override
	public SpringSupplierExtension createService(ServiceContext context) throws Throwable {

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

		// Obtain the default HTTP handler
		HttpHandler defaultHttpHandler = springContext.getBean(HttpHandler.class);

		// Obtain the default adapter
		HttpWebHandlerAdapter defaultAdapter = (defaultHttpHandler instanceof HttpWebHandlerAdapter)
				? (HttpWebHandlerAdapter) defaultHttpHandler
				: null;

		// Load the attributes for each procedure
		for (SpringWebFluxProcedure procedure : this.registeredProcedures) {

			// Build handler for procedure
			String[] beanNames = springContext.getBeanNamesForType(procedure.controllerClass);
			String beanName = (beanNames.length == 1) ? beanNames[0] : null;
			if (beanName == null) {
				throw new IllegalStateException("Found " + beanNames.length + " beans for type "
						+ procedure.controllerClass.getName() + " (expecting 1)");
			}

			// Obtain the method
			Method method = null;
			for (Method check : procedure.controllerClass.getMethods()) {
				if (check.getName().equals(procedure.controllerMethodName)) {
					method = check;
				}
			}
			if (method == null) {
				throw new IllegalStateException(
						"No " + HttpHandler.class.getSimpleName() + " for " + Controller.class.getSimpleName() + " "
								+ procedure.controllerClass.getName() + "#" + procedure.controllerMethodName);
			}

			// Find field to map handler mappings
			Field handlerMappings = DispatcherHandler.class.getDeclaredField("handlerMappings");
			handlerMappings.setAccessible(true);

			// Create the dispatcher handler
			DispatcherHandler dispatcherHandler = new DispatcherHandler(springContext);

			// Override handler for dispatcher to controller method
			HandlerMethod handlerMethod = new HandlerMethod(beanName, springContext, method);
			HandlerMapping handlerMapping = (exchange) -> Mono.just(handlerMethod.createWithResolvedBean());
			handlerMappings.set(dispatcherHandler, Arrays.asList(handlerMapping));

			// Create the HTTP handler
			HttpWebHandlerAdapter httpHandler = new HttpWebHandlerAdapter(dispatcherHandler) {

				@Override
				protected ServerWebExchange createExchange(ServerHttpRequest request, ServerHttpResponse response) {

					// Create the exchange
					ServerWebExchange exchange = super.createExchange(request, response);

					// Load the path parameters
					if (request instanceof OfficeFloorServerHttpRequest) {
						OfficeFloorServerHttpRequest officeFloorRequest = (OfficeFloorServerHttpRequest) request;
						officeFloorRequest.loadPathParameters(exchange);
					}

					// Return the exchange
					return exchange;
				}
			};
			httpHandler.setApplicationContext(springContext);
			if (defaultAdapter != null) {
				this.loadConfiguration(() -> defaultAdapter.getCodecConfigurer(),
						(configuration) -> httpHandler.setCodecConfigurer(configuration));
				this.loadConfiguration(() -> defaultAdapter.getForwardedHeaderTransformer(),
						(configuration) -> httpHandler.setForwardedHeaderTransformer(configuration));
				this.loadConfiguration(() -> defaultAdapter.getLocaleContextResolver(),
						(configuration) -> httpHandler.setLocaleContextResolver(configuration));
				this.loadConfiguration(() -> defaultAdapter.getSessionManager(),
						(configuration) -> httpHandler.setSessionManager(configuration));
			}

			// Specify the HTTP handler for procedure
			procedure.httpHandler = httpHandler;
		}

		// Stop registration
		registry.remove();
	}

	/**
	 * Loads the configuration.
	 * 
	 * @param <T>    Type.
	 * @param getter Getter.
	 * @param setter Setter.
	 */
	private <T> void loadConfiguration(Supplier<T> getter, Consumer<T> setter) {
		T configuration = getter.get();
		if (configuration != null) {
			setter.accept(configuration);
		}
	}

}
