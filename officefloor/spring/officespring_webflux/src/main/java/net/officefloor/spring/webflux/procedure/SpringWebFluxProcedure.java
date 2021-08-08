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

import java.lang.reflect.Method;

import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.stereotype.Controller;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.spring.webflux.WebFluxSectionSource;
import net.officefloor.web.state.HttpRequestState;

/**
 * Spring Web Flux {@link Controller} {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebFluxProcedure extends StaticManagedFunction<SpringWebFluxProcedure.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, HTTP_REQUEST_STATE
	}

	/**
	 * {@link Controller} {@link Class}.
	 */
	protected final Class<?> controllerClass;

	/**
	 * Name of {@link Controller} {@link Method}.
	 */
	protected final String controllerMethodName;

	/**
	 * {@link HttpHandler} loaded via the {@link SpringWebFluxProcedureRegistry}.
	 */
	protected HttpHandler httpHandler;

	/**
	 * Instantiate.
	 * 
	 * @param controllerClass      {@link Controller} {@link Class}.
	 * @param controllerMethodName Name of {@link Controller} {@link Method}.
	 */
	public SpringWebFluxProcedure(Class<?> controllerClass, String controllerMethodName) {
		this.controllerClass = controllerClass;
		this.controllerMethodName = controllerMethodName;
	}

	/*
	 * ======================= ManagedFunction ============================
	 */

	@Override
	public void execute(ManagedFunctionContext<DependencyKeys, None> context) throws Throwable {

		// Obtain dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(DependencyKeys.SERVER_HTTP_CONNECTION);
		HttpRequestState requestState = (HttpRequestState) context.getObject(DependencyKeys.HTTP_REQUEST_STATE);

		// Service
		AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
		WebFluxSectionSource.service(this.httpHandler, connection, requestState, asynchronousFlow, null);
	}

}
