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

package net.officefloor.spring.webflux;

import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.state.HttpRequestState;
import reactor.core.publisher.Mono;

/**
 * {@link SectionSource} servicing {@link ServerHttpConnection} via Web Flux.
 * 
 * @author Daniel Sagenschneider
 */
public class WebFluxSectionSource extends AbstractSectionSource {

	/**
	 * {@link DataBufferFactory}.
	 */
	private static final DataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();

	/**
	 * Services the {@link ServerHttpConnection} via the {@link HttpHandler}.
	 * 
	 * @param httpHandler      {@link HttpHandler}.
	 * @param connection       {@link ServerHttpConnection}.
	 * @param requestState     {@link HttpRequestState}.
	 * @param asynchronousFlow {@link AsynchronousFlow}.
	 * @param completion       {@link AsynchronousFlowCompletion}.
	 * @throws Exception If fails to service.
	 */
	public static void service(HttpHandler httpHandler, ServerHttpConnection connection, HttpRequestState requestState,
			AsynchronousFlow asynchronousFlow, AsynchronousFlowCompletion completion) throws Exception {

		// Create the request
		ServerHttpRequest request = new OfficeFloorServerHttpRequest(connection.getRequest(), requestState, "/",
				dataBufferFactory);

		// Create the response
		ServerHttpResponse response = new OfficeFloorServerHttpResponse(connection.getResponse(), dataBufferFactory);

		// Undertake servicing
		Mono<Void> mono = httpHandler.handle(request, response);
		mono.subscribe((success) -> {
			// Handled in complete

		}, (error) -> {
			// Provide failure
			asynchronousFlow.complete(() -> {
				throw error;
			});

		}, () -> {
			// Complete servicing
			asynchronousFlow.complete(completion);
		});
	}

	/**
	 * Specifies the {@link HttpHandler}.
	 * 
	 * @param httpHandler {@link HttpHandler}.
	 */
	public static void setHttpHandler(HttpHandler httpHandler) {
		WebFluxHttpHandler handler = webFluxHttpHandler.get();
		handler.function.httpHandler = httpHandler;
		handler.attemptRelease();
	}

	/**
	 * Manages {@link ThreadLocal} for {@link WebFluxFunction} setup.
	 */
	private static class WebFluxHttpHandler {

		/**
		 * {@link WebFluxFunction}.
		 */
		private final WebFluxFunction function = new WebFluxFunction();

		/**
		 * Indicates if {@link WebFluxFunction} used.
		 */
		private boolean isFunctionCreated = false;

		/**
		 * Attempts release of {@link ThreadLocal}.
		 */
		private void attemptRelease() {
			if ((function.httpHandler != null) && (this.isFunctionCreated)) {
				webFluxHttpHandler.remove();
			}
		}
	}

	/**
	 * {@link ThreadLocal} for the {@link WebFluxHttpHandler}.
	 */
	private static final ThreadLocal<WebFluxHttpHandler> webFluxHttpHandler = new ThreadLocal<WebFluxHttpHandler>() {

		@Override
		protected WebFluxHttpHandler initialValue() {
			return new WebFluxHttpHandler();
		}
	};

	/**
	 * {@link SectionInput} name for servicing the {@link ServerHttpConnection}.
	 */
	public static final String INPUT = "serviceBySpringWeb";

	/**
	 * {@link SectionOutput} for passing on to next in chain for servicing
	 * {@link ServerHttpConnection}.
	 */
	public static final String OUTPUT = "notServiced";

	/**
	 * Name of service {@link ManagedFunction}.
	 */
	private static final String FUNCTION = "service";

	/*
	 * ========================= SectionSource ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// no specification
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Configure in servicing
		SectionFunction service = designer.addSectionFunctionNamespace(FUNCTION, new WebFluxManagedFunctionSource())
				.addSectionFunction(FUNCTION, FUNCTION);

		// Link for use
		designer.link(designer.addSectionInput(INPUT, null), service);
		designer.link(service.getFunctionFlow(FlowKeys.NOT_FOUND.name()),
				designer.addSectionOutput(OUTPUT, null, false), false);

		// Provide dependencies
		designer.link(service.getFunctionObject(DependencyKeys.SERVER_HTTP_CONNECTION.name()), designer
				.addSectionObject(ServerHttpConnection.class.getSimpleName(), ServerHttpConnection.class.getName()));
		designer.link(service.getFunctionObject(DependencyKeys.HTTP_REQUEST_STATE.name()),
				designer.addSectionObject(HttpRequestState.class.getSimpleName(), HttpRequestState.class.getName()));
	}

	/**
	 * Dependency keys.
	 */
	private static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, HTTP_REQUEST_STATE
	}

	/**
	 * Flow keys.
	 */
	private static enum FlowKeys {
		NOT_FOUND
	}

	/**
	 * {@link ManagedFunctionSource} for {@link WebFluxFunction}.
	 */
	private static class WebFluxManagedFunctionSource extends AbstractManagedFunctionSource {

		/*
		 * ===================== ManagedFunctionSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// no specification
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Create the web flux function
			WebFluxHttpHandler handler = webFluxHttpHandler.get();
			if (!context.isLoadingType()) {
				handler.isFunctionCreated = true;
				handler.attemptRelease();
			}

			// Provide service function
			ManagedFunctionTypeBuilder<DependencyKeys, FlowKeys> function = functionNamespaceTypeBuilder
					.addManagedFunctionType(FUNCTION, DependencyKeys.class, FlowKeys.class)
					.setFunctionFactory(handler.function);
			function.addObject(ServerHttpConnection.class).setKey(DependencyKeys.SERVER_HTTP_CONNECTION);
			function.addObject(HttpRequestState.class).setKey(DependencyKeys.HTTP_REQUEST_STATE);
			function.addFlow().setKey(FlowKeys.NOT_FOUND);
		}
	}

	/**
	 * Web Flux {@link ManagedFunction}.
	 */
	private static class WebFluxFunction extends StaticManagedFunction<DependencyKeys, FlowKeys> {

		/**
		 * {@link HttpHandler}.
		 */
		private HttpHandler httpHandler;

		/*
		 * ======================== ManagedFunction =========================
		 */

		@Override
		public void execute(ManagedFunctionContext<DependencyKeys, FlowKeys> context) throws Throwable {

			// Obtain dependencies
			ServerHttpConnection connection = (ServerHttpConnection) context
					.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
			HttpRequestState requestState = (HttpRequestState) context.getObject(DependencyKeys.HTTP_REQUEST_STATE);

			// Service
			AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
			HttpResponse httpResponse = connection.getResponse();
			WebFluxSectionSource.service(this.httpHandler, connection, requestState, asynchronousFlow, () -> {
				if (HttpStatus.NOT_FOUND.equals(httpResponse.getStatus())) {

					// Reset and attempt further handling in chain
					httpResponse.reset();
					context.doFlow(FlowKeys.NOT_FOUND, null, null);
				}
			});
		}
	}

}
