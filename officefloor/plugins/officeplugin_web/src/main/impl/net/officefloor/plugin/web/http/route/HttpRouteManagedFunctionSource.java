/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.web.http.route;

import java.io.IOException;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiator;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteTaskDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;

/**
 * <p>
 * {@link ManagedFunctionSource} to provide appropriately secure
 * {@link ServerHttpConnection}.
 * <p>
 * Configuration of what to secure is determined by
 * {@link HttpUrlContinuationDifferentiator} on the {@link Office}
 * {@link FunctionManager} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Name of the {@link HttpRouteFunction}.
	 */
	public static final String FUNCTION_NAME = "route";

	/*
	 * ==================== WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Configure the function
		ManagedFunctionTypeBuilder<HttpRouteTaskDependencies, HttpRouteTaskFlows> function = namespaceTypeBuilder
				.addManagedFunctionType(FUNCTION_NAME, new HttpRouteFunction(), HttpRouteTaskDependencies.class,
						HttpRouteTaskFlows.class);
		function.addObject(ServerHttpConnection.class).setKey(HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION);
		function.addObject(HttpApplicationLocation.class).setKey(HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION);
		function.addObject(HttpRequestState.class).setKey(HttpRouteTaskDependencies.REQUEST_STATE);
		function.addObject(HttpSession.class).setKey(HttpRouteTaskDependencies.HTTP_SESSION);
		function.addFlow().setKey(HttpRouteTaskFlows.NOT_HANDLED);
		function.addEscalation(InvalidHttpRequestUriException.class);
		function.addEscalation(HttpRequestTokeniseException.class);
		function.addEscalation(IOException.class);
		function.addEscalation(UnknownFunctionException.class);
		function.addEscalation(InvalidParameterTypeException.class);
	}

}