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
package net.officefloor.plugin.web.http.resource.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.resource.HttpDirectory;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.HttpResource;
import net.officefloor.plugin.web.http.resource.HttpResourceCreationListener;
import net.officefloor.plugin.web.http.resource.HttpResourceFactory;
import net.officefloor.plugin.web.http.resource.NotExistHttpResource;
import net.officefloor.plugin.web.http.resource.source.HttpFileFactoryFunction.DependencyKeys;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.state.HttpApplicationState;

/**
 * Tests the {@link HttpFileFactoryFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileFactoryManagedFunctionTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link HttpResourceFactory}.
	 */
	private HttpResourceFactory factory = this.createMock(HttpResourceFactory.class);

	/**
	 * Mock {@link HttpFile}.
	 */
	private final HttpFile httpFile = this.createMock(HttpFile.class);

	/**
	 * Mock {@link HttpResourceCreationListener}.
	 */
	@SuppressWarnings("unchecked")
	private final HttpResourceCreationListener<Indexed> creationListener = this
			.createMock(HttpResourceCreationListener.class);

	/**
	 * Ensures handle if {@link HttpFile} exists.
	 */
	public void testFileExists() throws Throwable {
		this.doFunctionTest("/path", true, "/path", false, this.httpFile);
	}

	/**
	 * Ensures handle non canonical path to {@link HttpFile}.
	 */
	public void testNonCanonicalPath() throws Throwable {
		this.doFunctionTest("/non-canonical/../path", true, "/path", false, this.httpFile);
	}

	/**
	 * Ensures handle non canonical path to {@link HttpFile}.
	 */
	public void testIncorrectContextForApplication() throws Throwable {
		NotExistHttpResource notExistResource = new NotExistHttpResource("/incorrect-context/path");
		this.doFunctionTest("/incorrect-context/path", false, "/path", false, notExistResource);
	}

	/**
	 * Ensures handle if {@link HttpFile} does not exist.
	 */
	public void testFileNotExists() throws Throwable {
		NotExistHttpResource notExistResource = new NotExistHttpResource("/path");
		this.doFunctionTest("/path", true, "/path", false, notExistResource);
	}

	/**
	 * Ensures handle if default {@link HttpFile}.
	 */
	public void testDefaultFile() throws Throwable {
		this.doFunctionTest("/path", true, "/path", true, this.httpFile);
	}

	/**
	 * Undertakes the test.
	 * 
	 * @param requestUri
	 *            Request URI.
	 * @param isValidContext
	 *            Indicates if valid context for application.
	 * @param filePath
	 *            File path.
	 * @param isDirectory
	 *            Indicates if directory.
	 * @param file
	 *            {@link HttpResource}.
	 * @return {@link HttpResource} returned from {@link ManagedFunction}.
	 */
	private void doFunctionTest(String requestUri, boolean isValidContext, String filePath, boolean isDirectory,
			HttpResource file) throws Throwable {

		// Create the mock
		@SuppressWarnings("unchecked")
		ManagedFunctionContext<DependencyKeys, Indexed> functionContext = this.createMock(ManagedFunctionContext.class);

		// Create the connection
		ServerHttpConnection connection = MockHttpServer.mockConnection(MockHttpServer.mockRequest(requestUri));
		HttpApplicationState applicationState = MockWebApp.mockApplicationState(null);

		// Record
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.SERVER_HTTP_CONNECTION),
				connection);
		this.recordReturn(functionContext, functionContext.getObject(DependencyKeys.HTTP_APPLICATION_STATE),
				applicationState);
		if (isValidContext) {
			// Valid context
			if (isDirectory) {
				// Record directory
				final HttpDirectory directory = this.createMock(HttpDirectory.class);
				this.recordReturn(this.factory, this.factory.createHttpResource(filePath), directory);
				this.recordReturn(directory, directory.getDefaultFile(), file);
			} else {
				// Record file
				this.recordReturn(this.factory, this.factory.createHttpResource(filePath), file);
			}
		} else {
			// Record not exist
			this.recordReturn(this.factory, this.factory.createHttpResource(requestUri), file);
		}
		this.creationListener.httpResourceCreated(file, connection, functionContext);

		// Test
		this.replayMockObjects();
		HttpFileFactoryFunction<Indexed> function = new HttpFileFactoryFunction<Indexed>(this.factory,
				this.creationListener);
		HttpResource resource = (HttpResource) function.execute(functionContext);
		this.verifyMockObjects();
		assertEquals("Incorrect returned HTTP file", file, resource);
	}

}