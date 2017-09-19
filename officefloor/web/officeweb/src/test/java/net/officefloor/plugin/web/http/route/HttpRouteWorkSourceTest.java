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
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteFunctionDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteFunction.HttpRouteFunctionFlows;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Tests the {@link HttpRouteManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpRouteManagedFunctionSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		HttpRouteFunction factory = new HttpRouteFunction();
		ManagedFunctionTypeBuilder<HttpRouteFunctionDependencies, HttpRouteFunctionFlows> function = type
				.addManagedFunctionType(HttpRouteManagedFunctionSource.FUNCTION_NAME, factory,
						HttpRouteFunctionDependencies.class, HttpRouteFunctionFlows.class);
		function.addObject(ServerHttpConnection.class).setKey(HttpRouteFunctionDependencies.SERVER_HTTP_CONNECTION);
		function.addObject(HttpApplicationLocation.class)
				.setKey(HttpRouteFunctionDependencies.HTTP_APPLICATION_LOCATION);
		function.addObject(HttpRequestState.class).setKey(HttpRouteFunctionDependencies.REQUEST_STATE);
		function.addObject(HttpSession.class).setKey(HttpRouteFunctionDependencies.HTTP_SESSION);
		function.addFlow().setKey(HttpRouteFunctionFlows.NOT_HANDLED);
		function.addEscalation(InvalidHttpRequestUriException.class);
		function.addEscalation(HttpRequestTokeniseException.class);
		function.addEscalation(IOException.class);
		function.addEscalation(UnknownFunctionException.class);
		function.addEscalation(InvalidParameterTypeException.class);

		// Validate the expected type
		ManagedFunctionLoaderUtil.validateManagedFunctionType(type, HttpRouteManagedFunctionSource.class);
	}

}