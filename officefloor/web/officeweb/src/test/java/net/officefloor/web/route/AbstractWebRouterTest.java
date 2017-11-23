/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.route;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpRequestBuilder;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.server.http.mock.MockServerHttpConnection;
import net.officefloor.web.HttpInputPath;
import net.officefloor.web.build.HttpPathFactory;
import net.officefloor.web.state.HttpArgument;

/**
 * Tests the {@link WebRouter}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWebRouterTest extends OfficeFrameTestCase {

	/**
	 * Obtains the context path.
	 * 
	 * @return Context path.
	 */
	protected abstract String getContextPath();

	/**
	 * {@link ManagedFunctionContext}.
	 */
	private static ManagedFunctionContext<?, Indexed> managedFunctionContext;

	/**
	 * Ensure can route root (/) path.
	 */
	public void testRootPath() {
		this.route("/", T("/"));
	}

	/**
	 * Ensure can POST to root (/).
	 */
	public void testPostRoot() {
		this.route(HttpMethod.POST, "/", T(HttpMethod.POST, "/"));
	}

	/**
	 * Ensure can route static path.
	 */
	public void testStaticPath() {
		this.route("/path", T("/path"));
	}

	/**
	 * Ensure can route static path with query string.
	 */
	public void testStaticPathWithQueryString() {
		this.route("/path?query=string", T("/path"));
	}

	/**
	 * Ensure can POST static path with query string.
	 */
	public void testPostPathWithQueryString() {
		this.route(HttpMethod.POST, "/path?query=string", T(HttpMethod.POST, "/path"));
	}

	/**
	 * Should not occur, but including fragment for completeness.
	 */
	public void testStaticPathWithFragment() {
		this.route("/path#fragment", T("/path"));
	}

	/**
	 * Ensure matches the longer path.
	 */
	public void testMatchLongerStaticPath() {
		this.route("/path/longer", R("/path"), T("/path/longer"));
	}

	/**
	 * Ensure matches the longer POST path.
	 */
	public void testPostLongStaticPath() {
		this.route(HttpMethod.POST, "/path/longer", R(HttpMethod.POST, "/path"), T(HttpMethod.POST, "/path/longer"));
	}

	/**
	 * Ensure static route to path can ignore ending slash.
	 */
	public void testStaticPathMatchEndingSlash() {
		this.route("/path/", T("/path"));
	}

	/**
	 * Ensure can route with path parameter.
	 */
	public void testParamSuffix() {
		this.route("/path/value", T("/path/{param}", "param", "value"));
	}

	/**
	 * Ensure can POST with path parameter.
	 */
	public void testPostParamSuffix() {
		this.route(HttpMethod.POST, "/path/value", T(HttpMethod.POST, "/path/{param}", "param", "value"));
	}

	/**
	 * Ensure ignore ending / for param value.
	 */
	public void testIgnoreEndingSlashForParam() {
		this.route("/path/value/", T("/path/{param}", "param", "value"));
	}

	/**
	 * Ensure path parameter terminated by query string.
	 */
	public void testParamTermintedByQueryString() {
		this.route("/path/value?query=string", T("/path/{param}", "param", "value"));
	}

	/**
	 * Ensure POST path parameter terminated by query string.
	 */
	public void testPostParamTermintedByQueryString() {
		this.route(HttpMethod.POST, "/path/value?query=string", T(HttpMethod.POST, "/path/{param}", "param", "value"));
	}

	/**
	 * Ensure path parameter terminated by fragment. Should not occur, but
	 * included for completeness.
	 */
	public void testParamTermintedByFragment() {
		this.route("/path/value#fragment", T("/path/{param}", "param", "value"));
	}

	/**
	 * Prefix with parameter in the path.
	 */
	public void testParamPrefix() {
		this.route("/value/path", T("/{param}/path", "param", "value"));
	}

	/**
	 * Prefix with parameter in the POST path.
	 */
	public void testPostParamPrefix() {
		this.route(HttpMethod.POST, "/value/path", T(HttpMethod.POST, "/{param}/path", "param", "value"));
	}

	/**
	 * Ensure parameter need not be path segment (but can be mixed in with other
	 * string values).
	 */
	public void testMixInParameter() {
		this.route("/mix-value-in", T("/mix-{param}-in", "param", "value"));
	}

	/**
	 * Ensure can have multiple path parameters.
	 */
	public void testMultipleParameters() {
		this.route("/one-1-two-2/3", T("/one-{one}-two-{two}/{three}", "one", "1", "two", "2", "three", "3"));
	}

	/**
	 * Ensure match the more static path first.
	 */
	public void testMatchMoreStaticParameterPath() {
		this.route("/path/value-static", R("/path/{param}"), T("/path/{param}-static", "param", "value"));
	}

	/**
	 * <p>
	 * Ensure not greedy in matching values.
	 * <p>
	 * Note: static content can not be part of the parameter value.
	 */
	public void testNonGreedyMatchLeftToRight() {
		this.route("/path/value12static", R("/path/{param}2{another}"),
				T("/path/{param}1{another}", "param", "value", "another", "2static"));
	}

	/**
	 * <p>
	 * Ensure not greedy in matching POST values.
	 * <p>
	 * Note: static content can not be part of the parameter value.
	 */
	public void testPostNonGreedyMatchLeftToRight() {
		this.route(HttpMethod.POST, "/path/value12static", R(HttpMethod.POST, "/path/{param}2{another}"),
				T(HttpMethod.POST, "/path/{param}1{another}", "param", "value", "another", "2static"));
	}

	/**
	 * Ensure if back out down one route that parameters are not included for
	 * that route.
	 */
	public void testBackoutParameters() {
		this.route("/path/value12static/backout",
				R("/path/{param}1{two}/not/match/but/longer/static/match/so/first/route/checked"),
				T("/path/{param}2{second}/backout", "param", "value1", "second", "static"));
	}

	/**
	 * Ensure if back out down one route that parameters are not included for
	 * that POST route.
	 */
	public void testPostBackoutParameters() {
		this.route(HttpMethod.POST, "/path/value12static/backout",
				R(HttpMethod.POST, "/path/{param}1{two}/not/match/but/longer/static/match/so/first/route/checked"),
				T(HttpMethod.POST, "/path/{param}2{second}/backout", "param", "value1", "second", "static"));
	}

	/**
	 * Ensure can ignore prefix of path only (ignoring rest of path in match).
	 */
	public void testMatchingIgnoringRemaining() {
		this.route("/prefix/extra", T("/prefix{}", "", "/extra"));
	}

	/**
	 * Ensure indicate that method not allowed.
	 */
	public void testMethodNotAllowed() {
		MockServerHttpConnection connection = this.route(HttpMethod.POST, "/", Ti("/"));
		MockHttpResponse response = connection.send(null);
		assertEquals("Should not allow POST", 405, response.getStatus().getStatusCode());
		assertEquals("Should indicate available methods", "GET, HEAD, OPTIONS", response.getHeader("Allow").getValue());
	}

	/**
	 * Ensure indicate method not allowed when matching with path parameter.
	 */
	public void testMethodNotAllowedWithPathParameter() {
		MockServerHttpConnection connection = this.route(HttpMethod.GET, "/value", Ti(HttpMethod.POST, "/{param}"));
		MockHttpResponse response = connection.send(null);
		assertEquals("Should not allow GET", 405, response.getStatus().getStatusCode());
		assertEquals("Should indicate available methods", "OPTIONS, POST", response.getHeader("Allow").getValue());
	}

	/**
	 * Ensure can create static path.
	 */
	public void testPathStatic() throws Exception {
		this.pathFactory("/path", null, "/path");
	}

	/**
	 * Ensure can create dynamic path containing path parameters.
	 */
	public void testPathDynamic() throws Exception {
		this.pathFactory("/{param}", new Values(), "/value");
	}

	/**
	 * Ensure can create path with sub properties.
	 */
	public void testPathSubProperty() throws Exception {
		this.pathFactory("/{subValue.value}", new Values(), "/sub");
	}

	/**
	 * Ensure can create path with multiple parameters.
	 */
	public void testPathMultipleParameters() throws Exception {
		this.pathFactory("/test-{subValue.value}-another/property-{param}", new Values(),
				"/test-sub-another/property-value");
	}

	/**
	 * Ensure notified path parameters.
	 */
	public void testPathMissingParameter() throws Exception {
		try {
			this.pathFactory("/{missing}", null, null);
		} catch (HttpException ex) {
			assertEquals("Incorrect cause",
					"For path '/{missing}', no property 'missing' on object " + Object.class.getName(), ex.getEntity());
		}
	}

	/**
	 * Ensure notified path parameters.
	 */
	public void testPathMissingSomeParameters() throws Exception {
		try {
			this.pathFactory("/{param}/{missing}", new Values(), null);
		} catch (HttpException ex) {
			assertEquals("Incorrect cause",
					"For path '/{param}/{missing}', no property 'missing' on object " + Values.class.getName(),
					ex.getEntity());
		}
	}

	public static class Values {
		public String getParam() {
			return "value";
		}

		public SubValues getSubValue() {
			return new SubValues();
		}
	}

	public static class SubValues {
		public String getValue() {
			return "sub";
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		managedFunctionContext = this.createMock(ManagedFunctionContext.class);
	}

	@SuppressWarnings("unchecked")
	public <T> void pathFactory(String routePath, T values, String expectedPath) throws Exception {

		// Create the builder and build route
		WebRouterBuilder builder = new WebRouterBuilder(this.getContextPath());
		HttpInputPath inputPath = builder.addRoute(HttpMethod.GET, routePath, null);

		// Create the path factory
		HttpPathFactory<T> httpPathFactory = inputPath
				.createHttpPathFactory((Class<T>) ((values == null) ? Object.class : values.getClass()));
		String path = httpPathFactory.createApplicationClientPath(values);

		// Validate the expected path
		String contextPath = this.getContextPath();
		if (contextPath != null) {
			expectedPath = contextPath + expectedPath;
		}
		assertEquals("Incorrect path", expectedPath, path);
	}

	public MockServerHttpConnection route(String path, MockWebRoute... routes) {
		return this.route(HttpMethod.GET, path, routes);
	}

	public MockServerHttpConnection route(HttpMethod method, String path, MockWebRoute... routes) {

		// Determine if should be handled
		boolean isHandled = false;
		for (MockWebRoute route : routes) {
			if (route.isHandler) {
				assertFalse("Invalid test. Should only have one handling route", isHandled);
				isHandled = true;
			}
		}

		// Build the web router (from routes)
		String contextPath = this.getContextPath();
		WebRouterBuilder builder = new WebRouterBuilder(contextPath);
		for (MockWebRoute route : routes) {
			HttpInputPath inputPath = builder.addRoute(route.method, route.path, route);
			boolean isParameter = route.path.contains("{");
			assertEquals("Route " + route.path + " incorrect indication of path parameters", isParameter,
					inputPath.isPathParameters());
		}
		WebRouter router = builder.build();

		// Undertake the route
		if (contextPath != null) {
			path = contextPath + path;
		}
		MockHttpRequestBuilder request = MockHttpServer.mockRequest(path).method(method);
		MockServerHttpConnection connection = MockHttpServer.mockConnection(request);
		boolean isServiced = router.service(connection, managedFunctionContext);

		// Determine if appropriately serviced
		assertEquals("Invalid servicing", isHandled, isServiced);

		// Ensure not handle incorrectly
		for (MockWebRoute route : routes) {
			assertEquals("Invalid route handling of path " + route.path, route.isHandler, route.isInvoked);
		}

		// Return the connection
		return connection;
	}

	private static MockWebRoute R(String path) {
		return new MockWebRoute(HttpMethod.GET, path, false);
	}

	private static MockWebRoute R(HttpMethod method, String path) {
		return new MockWebRoute(method, path, false);
	}

	private static MockWebRoute T(String path, String... pathArgumentNameValuePairs) {
		return new MockWebRoute(HttpMethod.GET, path, true, pathArgumentNameValuePairs);
	}

	private static MockWebRoute T(HttpMethod method, String path, String... pathArgumentNameValuePairs) {
		return new MockWebRoute(method, path, true, pathArgumentNameValuePairs);
	}

	private static MockWebRoute Ti(String path) {
		MockWebRoute route = T(path);
		route.isInvoked = true; // target but issue so not serviced
		return route;
	}

	private static MockWebRoute Ti(HttpMethod method, String path) {
		MockWebRoute route = T(method, path);
		route.isInvoked = true; // target but issue so not serviced
		return route;
	}

	private static class MockWebRoute implements WebRouteHandler {

		private final HttpMethod method;

		private final String path;

		private final boolean isHandler;

		private boolean isInvoked = false;

		private final String[] pathArgumentNameValuePairs;

		private MockWebRoute(HttpMethod method, String path, boolean isHandler, String... pathArgumentNameValuePairs) {
			this.method = method;
			this.path = path;
			this.isHandler = isHandler;
			this.pathArgumentNameValuePairs = pathArgumentNameValuePairs;
		}

		/*
		 * ============== WebRouteHandler ======================
		 */

		@Override
		public void handle(HttpArgument pathArguments, ManagedFunctionContext<?, Indexed> context) {

			// Ensure should be handling route
			assertTrue("Should not handle route (" + this.path + ")", this.isHandler);
			this.isInvoked = true;

			// Validate the path arguments
			for (int i = 0; i < this.pathArgumentNameValuePairs.length; i += 2) {
				String expectedName = this.pathArgumentNameValuePairs[i];
				String expectedValue = this.pathArgumentNameValuePairs[i + 1];
				assertNotNull("Invalid number of path arguments (expecting " + expectedName + ")", pathArguments);
				assertEquals("Incorrect argument name", expectedName, pathArguments.name);
				assertEquals("Incorrect argument value", expectedValue, pathArguments.value);

				// Set up for next argument
				pathArguments = pathArguments.next;
			}
			assertNull("Should be no further path arguments", pathArguments);

			// Ensure correct context
			assertEquals("Incorrect context", managedFunctionContext, context);
		}
	}

}