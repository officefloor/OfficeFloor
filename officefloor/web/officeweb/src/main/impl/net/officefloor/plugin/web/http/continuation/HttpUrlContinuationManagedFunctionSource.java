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

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * {@link ManagedFunctionSource} for a HTTP URL continuation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationManagedFunctionSource extends AbstractManagedFunctionSource {

	/**
	 * Name of {@link Property} specifying the URI path for the HTTP URL
	 * continuation.
	 */
	public static final String PROPERTY_URI_PATH = "http.continuation.uri.path";

	/**
	 * Name of {@link Property} specifying the {@link HttpMethod} for the HTTP
	 * URL continuation.
	 */
	public static final String PROPERTY_HTTP_METHOD = "http.continutation.method";

	/**
	 * Name of {@link Property} specifying whether the HTTP URL continuation
	 * requires a secure {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_SECURE = "http.continuation.secure";

	/**
	 * Name of the {@link ManagedFunctionType}.
	 */
	public static final String FUNCTION_NAME = "CONTINUATION";

	/**
	 * Obtains the application URI path from the configured URI path.
	 * 
	 * @param configuredUriPath
	 *            Configured URI path.
	 * @return Application URI path.
	 */
	public static String getApplicationUriPath(String configuredUriPath) {

		// Ensure the configure URI path is absolute
		String applicationUriPath = (configuredUriPath.startsWith("/") ? configuredUriPath : "/" + configuredUriPath);

		// Ensure is canonical
		try {
			applicationUriPath = HttpApplicationLocationMangedObject.transformToCanonicalPath(applicationUriPath);
		} catch (HttpException ex) {
			// Use path as is
		}

		// Return the Application URI path
		return applicationUriPath;
	}

	/*
	 * ====================== ManagedFunctionSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_URI_PATH, "URI Path");
		context.addProperty(PROPERTY_HTTP_METHOD, "Method");
	}

	@Override
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the application URI path (for use)
		String applicationUriPath = context.getProperty(PROPERTY_URI_PATH);
		applicationUriPath = getApplicationUriPath(applicationUriPath);

		// Obtain the HTTP method
		String methodName = context.getProperty(PROPERTY_HTTP_METHOD);
		HttpMethod method = HttpMethod.getHttpMethod(methodName);

		// Determine if secure
		String isSecureText = context.getProperty(PROPERTY_SECURE, null);
		Boolean isSecure = (isSecureText == null ? null : Boolean.valueOf(isSecureText));

		// Create the annotation
		HttpUrlContinuationAnnotation annotation = new HttpUrlContinuationAnnotationImpl(method, applicationUriPath,
				isSecure);

		// Create the factory
		HttpUrlContinuationFunction factory = new HttpUrlContinuationFunction();

		// Configure the function
		namespaceTypeBuilder.addManagedFunctionType(FUNCTION_NAME, factory, None.class, None.class)
				.addAnnotation(annotation);
	}
}