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
package net.officefloor.plugin.web.http.parameters.source;

import java.io.IOException;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.conversation.impl.HttpRequestImpl;
import net.officefloor.plugin.web.http.parameters.HttpParametersException;
import net.officefloor.plugin.web.http.parameters.source.HttpParametersLoaderManagedFunctionSource.HttpParametersLoaderFunction;

/**
 * Tests the {@link HttpParametersLoaderManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validates the specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpParametersLoaderManagedFunctionSource.class,
				HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME,
				HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME);
	}

	/**
	 * Validates the type.
	 */
	public void testType() {
		FunctionNamespaceBuilder namespaceBuilder = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		HttpParametersLoaderFunction function = new HttpParametersLoaderManagedFunctionSource().new HttpParametersLoaderFunction();
		ManagedFunctionTypeBuilder<HttpParametersLoaderDependencies, None> functionBuilder = namespaceBuilder
				.addManagedFunctionType("LOADER", function, HttpParametersLoaderDependencies.class, None.class);
		functionBuilder.addObject(ServerHttpConnection.class)
				.setKey(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION);
		functionBuilder.addObject(MockType.class).setKey(HttpParametersLoaderDependencies.OBJECT);
		functionBuilder.setReturnType(MockType.class);
		functionBuilder.addEscalation(IOException.class);
		functionBuilder.addEscalation(HttpParametersException.class);
		ManagedFunctionLoaderUtil.validateManagedFunctionType(namespaceBuilder,
				HttpParametersLoaderManagedFunctionSource.class,
				HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME, MockType.class.getName());
	}

	/**
	 * Validates can source {@link FunctionNamespaceType} and execute the
	 * {@link ManagedFunction}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testDoTask() throws Throwable {

		// Record executing task (loading object)
		ManagedFunctionContext taskContext = this.createMock(ManagedFunctionContext.class);
		ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
		this.recordReturn(taskContext, taskContext.getObject(HttpParametersLoaderDependencies.SERVER_HTTP_CONNECTION),
				connection);
		MockType object = this.createMock(MockType.class);
		this.recordReturn(taskContext, taskContext.getObject(HttpParametersLoaderDependencies.OBJECT), object);
		HttpRequest request = new HttpRequestImpl("GET", "/path?VALUE=value", "HTTP/1.1", null, null);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		object.setValue("value");

		// Test
		this.replayMockObjects();
		FunctionNamespaceType namespaceType = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpParametersLoaderManagedFunctionSource.class,
				HttpParametersLoaderManagedFunctionSource.PROPERTY_TYPE_NAME, MockType.class.getName());
		ManagedFunction<?, ?> function = namespaceType.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();
		Object result = function.execute(taskContext);
		assertEquals("Incorrect resulting object", object, result);
		this.verifyMockObjects();
	}

	/**
	 * Type for testing.
	 */
	public static interface MockType {

		void setValue(String value);
	}

}