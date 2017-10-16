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
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.state.HttpArgument;

/**
 * Tests the {@link WebRouter}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebRouterTest extends OfficeFrameTestCase {

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
		this.route("/one-1-two-2/3", T("/one-{one}-two-{two}/{three}", "three", "3", "two", "2", "one", "1"));
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
				T("/path/{param}1{another}", "another", "2static", "param", "value"));
	}

	/**
	 * Ensure if back out down one route that parameters are not included for
	 * that route.
	 */
	public void testBackoutParameters() {
		this.route("/path/value12static/backout",
				R("/path/{param}1{two}/not/match/but/longer/static/match/so/first/route/checked"),
				T("/path/{param}2{second}/backout", "second", "static", "param", "value1"));
	}

	/**
	 * Ensure can ignore prefix of path only (ignoring rest of path in match).
	 */
	public void testMatchingIgnoringRemaining() {
		this.route("/prefix/extra", T("/prefix{}", "", "/extra"));
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		managedFunctionContext = this.createMock(ManagedFunctionContext.class);
	}

	public void route(String path, MockWebRoute... routes) {

		// Determine if should be handled
		boolean isHandled = false;
		for (MockWebRoute route : routes) {
			if (route.isHandler) {
				assertFalse("Invalid test. Should only have one handling route", isHandled);
				isHandled = true;
			}
		}

		// Build the web router (from routes)
		WebRouterBuilder builder = new WebRouterBuilder(null);
		for (MockWebRoute route : routes) {
			builder.addRoute(HttpMethod.GET, route.path, route);
		}
		WebRouter router = builder.build();

		// Undertake the route
		boolean isServiced = router.service(MockHttpServer.mockRequest(path).build(), managedFunctionContext);

		// Determine if appropriately serviced
		assertEquals("Invalid servicing", isHandled, isServiced);

		// Ensure not handle incorrectly
		for (MockWebRoute route : routes) {
			assertEquals("Invalid route handling of path " + route.path, route.isHandler, route.isInvoked);
		}
	}

	private static MockWebRoute R(String path) {
		return new MockWebRoute(path, false);
	}

	private static MockWebRoute T(String path, String... pathArgumentNameValuePairs) {
		return new MockWebRoute(path, true, pathArgumentNameValuePairs);
	}

	private static class MockWebRoute implements WebRouteHandler {

		private final String path;

		private final boolean isHandler;

		private boolean isInvoked = false;

		private final String[] pathArgumentNameValuePairs;

		private MockWebRoute(String path, boolean isHandler, String... pathArgumentNameValuePairs) {
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