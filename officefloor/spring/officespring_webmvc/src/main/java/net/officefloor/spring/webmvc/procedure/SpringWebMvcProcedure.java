/*-
 * #%L
 * Spring Web MVC Integration
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

package net.officefloor.spring.webmvc.procedure;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.HandlerMapping;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.servlet.ServletServicer;
import net.officefloor.web.build.HttpValueLocation;
import net.officefloor.web.state.HttpRequestState;

/**
 * Spring Web MVC {@link Controller} {@link Procedure}.
 * 
 * @author Daniel Sagenschneider
 */
public class SpringWebMvcProcedure extends StaticManagedFunction<SpringWebMvcProcedure.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		SERVER_HTTP_CONNECTION, HTTP_REQUEST_STATE, SERVLET_SERVICER
	}

	/**
	 * {@link ServletServicer} for the {@link ProcedureProxyServlet}.
	 */
	private final ServletServicer servletServicer;

	/**
	 * {@link Controller} {@link Class}.
	 */
	protected final Class<?> controllerClass;

	/**
	 * Name of {@link Controller} {@link Method}.
	 */
	protected final String controllerMethodName;

	/**
	 * Attributes for {@link ServletServicer}. Provided by
	 * {@link SpringWebMvcProcedureRegistry}.
	 */
	protected Map<String, Object> attributes;

	/**
	 * Instantiate.
	 * 
	 * @param servletServicer      {@link ServletServicer} for the
	 *                             {@link ProcedureDispatcherServlet}.
	 * @param controllerClass      {@link Controller} {@link Class}.
	 * @param controllerMethodName Name of {@link Controller} {@link Method}.
	 */
	public SpringWebMvcProcedure(ServletServicer servletServicer, Class<?> controllerClass,
			String controllerMethodName) {
		this.servletServicer = servletServicer;
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

		// Load the path parameters
		Map<String, String> pathParameters = new HashMap<>();
		requestState.loadValues((name, value, location) -> {
			if (location == HttpValueLocation.PATH) {
				pathParameters.put(name, value);
			}
		});

		// Create request attributes
		Map<String, Object> requestAttributes = new HashMap<>(this.attributes.size() + 1);
		requestAttributes.putAll(this.attributes);
		requestAttributes.put(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, pathParameters);

		// Service
		AsynchronousFlow asynchronousFlow = context.createAsynchronousFlow();
		Executor executor = context.getExecutor();
		this.servletServicer.service(connection, executor, asynchronousFlow, null, requestAttributes);
	}

}
