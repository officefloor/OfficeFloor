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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiator;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiatorImpl;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.route.HttpRouteTaskTest;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialTask.Dependencies;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialTask.Flows;

/**
 * Tests the {@link HttpTemplateInitialWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Validate specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil
				.validateSpecification(HttpTemplateInitialWorkSource.class,
						HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI,
						"URI Path");
	}

	/**
	 * Validate the non-secure type by default.
	 */
	public void testDefaultNonSecureType() {
		this.doTypeTest(null, null, "/path", null, "/path");
	}

	/**
	 * Validate the non-secure type.
	 */
	public void testNonSecureType() {
		this.doTypeTest(Boolean.FALSE, null, "/path", null, "/path");
	}

	/**
	 * Validate the secure type.
	 */
	public void testSecureType() {
		this.doTypeTest(Boolean.TRUE, Boolean.TRUE, "/path", null, "/path");
	}

	/**
	 * Validates uses canonical path for URL continuation.
	 */
	public void testNonCanonicalPathType() {
		this.doTypeTest(null, null, "configured/../non/../canoncial/../path",
				null, "/path");
	}

	/**
	 * Validate includes URI suffix.
	 */
	public void testTemplateUriSuffixType() {
		this.doTypeTest(null, null, "/path", ".suffix", "/path.suffix");
	}

	/**
	 * Validate root template type.
	 */
	public void testRootTemplateType() {
		this.doTypeTest(null, null, "/", null, "/");
	}

	/**
	 * Validate root template does not have suffix type.
	 */
	public void testRootTemplateNoSuffixType() {
		this.doTypeTest(null, null, "/", ".suffix", "/");
	}

	/**
	 * Undertakes the validating the type.
	 * 
	 * @param isSecure
	 *            Whether template should be secure.
	 */
	private void doTypeTest(Boolean isConfiguredSecure,
			Boolean isUrlContinuationSecure, String configuredUriPath,
			String uriSuffix, String expectedUrlContinuationPath) {

		// Factory
		HttpTemplateInitialTask factory = new HttpTemplateInitialTask(null,
				false, null, null, null);

		// Create the expected type
		FunctionNamespaceBuilder<HttpTemplateInitialTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);

		// Initial task
		ManagedFunctionTypeBuilder<Dependencies, Flows> initial = type.addManagedFunctionType("TASK",
				factory, Dependencies.class, Flows.class);
		initial.addObject(ServerHttpConnection.class).setKey(
				Dependencies.SERVER_HTTP_CONNECTION);
		initial.addObject(HttpApplicationLocation.class).setKey(
				Dependencies.HTTP_APPLICATION_LOCATION);
		initial.addObject(HttpRequestState.class).setKey(
				Dependencies.REQUEST_STATE);
		initial.addObject(HttpSession.class).setKey(Dependencies.HTTP_SESSION);
		initial.addFlow().setKey(Flows.RENDER);
		initial.addEscalation(IOException.class);
		initial.setDifferentiator(new HttpUrlContinuationDifferentiatorImpl(
				expectedUrlContinuationPath, isUrlContinuationSecure));

		// Create the listing of properties
		List<String> properties = new ArrayList<String>(6);
		properties.addAll(Arrays.asList(
				HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI,
				configuredUriPath));
		if (isConfiguredSecure != null) {
			properties.addAll(Arrays.asList(
					HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
					String.valueOf(isConfiguredSecure)));
		}
		if (uriSuffix != null) {
			properties.addAll(Arrays.asList(
					HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI_SUFFIX,
					uriSuffix));
		}

		// Validate type (must also convert
		FunctionNamespaceType<HttpTemplateInitialTask> work = WorkLoaderUtil
				.validateWorkType(type, HttpTemplateInitialWorkSource.class,
						properties.toArray(new String[properties.size()]));

		// Ensure correct URI path
		ManagedFunctionType<HttpTemplateInitialTask, ?, ?> task = work.getManagedFunctionTypes()[0];
		HttpUrlContinuationDifferentiator differentiator = (HttpUrlContinuationDifferentiator) task
				.getDifferentiator();
		assertEquals("Incorrect URI path", expectedUrlContinuationPath,
				differentiator.getApplicationUriPath());
		assertEquals("Incorrectly indentified as secure",
				isUrlContinuationSecure, differentiator.isSecure());
	}

	/**
	 * Ensure service as not required to be secure.
	 */
	public void testNonSecureService() {
		this.doServiceTest(false, false, "GET", null, null, null, null);
	}

	/**
	 * <p>
	 * Ensure service secure anyway if not require secure connection.
	 * <p>
	 * No need to redirect and establish a new connection to down grade secure
	 * when already received the request.
	 */
	public void testIgnoreSecureService() {
		this.doServiceTest(false, false, "GET", null, null, null, null);
	}

	/**
	 * Ensure service as appropriately secure.
	 */
	public void testSecureService() {
		this.doServiceTest(true, true, "GET", null, null, null, null);
	}

	/**
	 * Ensure redirect as not secure.
	 */
	public void testSecureRedirect() {
		this.doServiceTest(true, false, null, null, "/redirect", null, null);
	}

	/**
	 * Ensure follow POST/redirect/GET pattern even on secure connection to
	 * allow back button to work.
	 */
	public void testSecurePostRedirect() {
		this.doServiceTest(true, true, "POST", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow POST/redirect/GET pattern to allow back button to work.
	 */
	public void testPostRedirect() {
		this.doServiceTest(false, false, "POST", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow post/redirect/GET pattern to allow back button to work.
	 */
	public void testPostRedirectCaseInsensitive() {
		this.doServiceTest(false, false, "post", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow PUT/redirect/GET pattern to allow back button to work.
	 */
	public void testPutRedirect() {
		this.doServiceTest(false, false, "PUT", null, "/redirect", null, null);
	}

	/**
	 * Ensure follow ALTERNATE/redirect/GET pattern to allow back button to
	 * work.
	 */
	public void testAlternateRedirect() {
		this.doServiceTest(false, false, "OTHER", "POST, OTHER", "/redirect",
				null, null);
	}

	/**
	 * Ensure follow ALTERNATE/redirect/GET pattern to allow back button to
	 * work.
	 */
	public void testAlternateRedirectCaseInsensitive() {
		this.doServiceTest(false, false, "other", "Post, Other", "/redirect",
				null, null);
	}

	/**
	 * Ensure able to specify the Content-Type and {@link Charset}.
	 */
	public void testContentTypeAndCharset() {
		this.doServiceTest(false, false, "GET", null, null, "text/plain",
				Charset.defaultCharset());
	}

	/**
	 * Undertake service test.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void doServiceTest(boolean isRequireSecure,
			boolean isConnectionSecure, String method, String redirectMethods,
			String redirectUriPath, String contentType, Charset charset) {
		try {

			final ManagedFunctionContext context = this.createMock(ManagedFunctionContext.class);
			final ServerHttpConnection connection = this
					.createMock(ServerHttpConnection.class);
			final HttpRequestState requestState = this
					.createMock(HttpRequestState.class);
			final HttpSession session = this.createMock(HttpSession.class);
			final HttpApplicationLocation location = this
					.createMock(HttpApplicationLocation.class);
			final FlowFuture flowFuture = this.createMock(FlowFuture.class);

			// Create the task
			List<String> properties = new ArrayList<String>(6);
			properties.addAll(Arrays.asList(
					HttpTemplateInitialWorkSource.PROPERTY_TEMPLATE_URI,
					(redirectUriPath == null ? "/path" : redirectUriPath)));
			if (isRequireSecure) {
				properties.addAll(Arrays.asList(
						HttpTemplateWorkSource.PROPERTY_TEMPLATE_SECURE,
						String.valueOf(isRequireSecure)));
			}
			if (redirectMethods != null) {
				properties
						.addAll(Arrays
								.asList(HttpTemplateInitialWorkSource.PROPERTY_RENDER_REDIRECT_HTTP_METHODS,
										redirectMethods));
			}
			if (contentType != null) {
				properties.addAll(Arrays.asList(
						HttpTemplateInitialWorkSource.PROPERTY_CONTENT_TYPE,
						contentType));
			}
			if (charset != null) {
				properties.addAll(Arrays.asList(
						HttpTemplateInitialWorkSource.PROPERTY_CHARSET,
						charset.name()));
			}
			FunctionNamespaceType<HttpTemplateInitialTask> work = WorkLoaderUtil
					.loadWorkType(HttpTemplateInitialWorkSource.class,
							properties.toArray(new String[properties.size()]));
			ManagedFunction<HttpTemplateInitialTask, ?, ?> task = work.getManagedFunctionTypes()[0]
					.getManagedFunctionFactory().createManagedFunction(
							work.getWorkFactory().createWork());

			// Record obtaining the dependencies
			this.recordReturn(context,
					context.getObject(Dependencies.SERVER_HTTP_CONNECTION),
					connection);
			this.recordReturn(context,
					context.getObject(Dependencies.HTTP_APPLICATION_LOCATION),
					location);
			this.recordReturn(context,
					context.getObject(Dependencies.REQUEST_STATE), requestState);
			this.recordReturn(context,
					context.getObject(Dependencies.HTTP_SESSION), session);

			// Record determining if secure connection
			if (isRequireSecure) {
				this.recordReturn(connection, connection.isSecure(),
						isConnectionSecure);
			}

			// Record determining method for POST, redirect, GET pattern
			if (method != null) {
				this.recordReturn(connection, connection.getHttpMethod(),
						method);
			}

			// Record redirect or render
			if (redirectUriPath != null) {
				// Record necessary redirect
				HttpRouteTaskTest.recordDoRedirect(redirectUriPath,
						isRequireSecure, connection, requestState, session,
						location, this);
			} else {
				// Record triggering the render
				this.recordReturn(context, context.doFlow(Flows.RENDER, null),
						flowFuture);
			}

			// Record content type and charset
			final HttpResponse response = this.createMock(HttpResponse.class);
			if (contentType != null) {
				this.recordReturn(connection, connection.getHttpResponse(),
						response);
				response.setContentType(contentType, charset);
			}

			// Test
			this.replayMockObjects();
			task.execute(context);
			this.verifyMockObjects();

		} catch (Throwable ex) {
			fail(ex);
		}
	}

}