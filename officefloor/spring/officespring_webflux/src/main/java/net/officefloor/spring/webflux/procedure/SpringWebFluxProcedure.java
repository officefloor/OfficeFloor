/*-
 * #%L
 * Spring Web Flux Integration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
