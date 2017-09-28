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
package net.officefloor.plugin.web.http.continuation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpMethod;

/**
 * Tests the {@link HttpUrlContinuationManagedFunctionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationManagedFunctionSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		ManagedFunctionLoaderUtil.validateSpecification(HttpUrlContinuationManagedFunctionSource.class,
				HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, "URI Path");
	}

	/**
	 * Ensure secure URL continuation.
	 */
	public void testSecureType() {
		this.doTypeTest(HttpMethod.GET, "/path", "/path", Boolean.TRUE);
	}

	/**
	 * Ensure non-secure URL continuation.
	 */
	public void testNonSecureType() {
		this.doTypeTest(HttpMethod.GET, "/path", "/path", Boolean.FALSE);
	}

	/**
	 * Ensure always service URL continuation.
	 */
	public void testAlwaysServiceType() {
		this.doTypeTest(HttpMethod.GET, "/path", "/path", null);
	}

	/**
	 * Ensure URL continuation URI path is always absolute.
	 */
	public void testAbsolutePathType() {
		this.doTypeTest(HttpMethod.GET, "path", "/path", null);
	}

	/**
	 * Ensure URL continuation URI path is always canonical.
	 */
	public void testCanonicalPathType() {
		this.doTypeTest(HttpMethod.GET, "ignore/../path/", "/path", null);
	}

	/**
	 * Ensure URL continuation root URI path.
	 */
	public void testRootPathType() {
		this.doTypeTest(HttpMethod.GET, "/", "/", null);
	}

	/**
	 * Validate type.
	 */
	public void doTypeTest(HttpMethod httpMethod, String configuredUriPath, String expectedUriPath, Boolean isSecure) {

		// Create the expected type
		FunctionNamespaceBuilder type = ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
		HttpUrlContinuationFunction factory = new HttpUrlContinuationFunction();
		ManagedFunctionTypeBuilder<None, None> functionType = type.addManagedFunctionType(
				HttpUrlContinuationManagedFunctionSource.FUNCTION_NAME, factory, None.class, None.class);
		functionType.addAnnotation(new HttpUrlContinuationAnnotationImpl(httpMethod, expectedUriPath, isSecure));

		// Create the properties
		List<String> properties = new ArrayList<String>(4);
		properties.addAll(Arrays.asList(HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, configuredUriPath));
		if (isSecure != null) {
			properties.addAll(
					Arrays.asList(HttpUrlContinuationManagedFunctionSource.PROPERTY_SECURE, String.valueOf(isSecure)));
		}

		// Validate type
		FunctionNamespaceType namespace = ManagedFunctionLoaderUtil.validateManagedFunctionType(type,
				HttpUrlContinuationManagedFunctionSource.class, properties.toArray(new String[properties.size()]));

		// Validate the URL continuation
		ManagedFunctionType<?, ?> function = namespace.getManagedFunctionTypes()[0];
		HttpUrlContinuationAnnotation urlContinuation = (HttpUrlContinuationAnnotation) function.getAnnotations()[0];
		assertSame("Incorrect HTTP method on URL continuation", httpMethod, urlContinuation.getHttpMethod());
		assertEquals("Incorrect URI path on URL continuation", expectedUriPath,
				urlContinuation.getApplicationUriPath());
		assertEquals("Incorrect secure flag for URL continuation", isSecure, urlContinuation.isSecure());
	}

	/**
	 * Ensure can execute the {@link HttpUrlContinuationFunction}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void testFunction() throws Throwable {

		final ManagedFunctionContext context = this.createMock(ManagedFunctionContext.class);

		// Load the Task
		FunctionNamespaceType type = ManagedFunctionLoaderUtil.loadManagedFunctionType(
				HttpUrlContinuationManagedFunctionSource.class,
				HttpUrlContinuationManagedFunctionSource.PROPERTY_URI_PATH, "uri");
		ManagedFunction<?, ?> function = type.getManagedFunctionTypes()[0].getManagedFunctionFactory()
				.createManagedFunction();

		// Execute the function (should do nothing as always link by next)
		this.replayMockObjects();
		function.execute(context);
		this.verifyMockObjects();
	}

}