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
package net.officefloor.web.path;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpServerLocation;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.HttpServerLocationImpl;
import net.officefloor.web.escalation.BadRequestHttpException;
import net.officefloor.web.route.InvalidContextPathException;
import net.officefloor.web.route.WebRouter;
import net.officefloor.web.state.HttpApplicationState;
import net.officefloor.web.state.HttpApplicationStateManagedObjectSource;

/**
 * Provides listing of common tests for the various states of the
 * {@link HttpApplicationLocationMangedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractPathTestCase extends OfficeFrameTestCase {

	/**
	 * Obtains the context path for the test.
	 * 
	 * @return Context path.
	 */
	protected abstract String getContextPath();

	/**
	 * Ensure null path is defaulted to root.
	 */
	public void testApplicationPath_NullPath() throws Exception {
		this.doApplicationPathTest("/", null);
	}

	/**
	 * Ensure empty path is defaulted to root.
	 */
	public void testApplicationPath_EmptyPath() throws Exception {
		this.doApplicationPathTest("/", "");
	}

	/**
	 * Ensure blank path is defaulted to root.
	 */
	public void testApplicationPath_BlankPath() throws Exception {
		this.doApplicationPathTest("/", " ");
	}

	/**
	 * Ensure context path is root for application.
	 */
	public void testApplicationPath_ContextPath() throws Exception {
		this.doApplicationPathTest("/", "/CONTEXT");
	}

	/**
	 * Ensure leading white space is stripped off.
	 */
	public void testApplicationPath_TrimLeadingWhiteSpace() throws Exception {
		this.doApplicationPathTest("/path", "\t /CONTEXT/path");
	}

	/**
	 * Ensure trailing white space is stripped off.
	 */
	public void testApplicationPath_TrimTrailingWhiteSpace() throws Exception {
		this.doApplicationPathTest("/path", "/CONTEXT/path \n");
	}

	/**
	 * Ensure the same path is returned if already canonical.
	 */
	public void testApplicationPath_SameCanonicalPath() throws Exception {
		this.doApplicationPathTest("/path", "/CONTEXT/path");
	}

	/**
	 * Ensure trailing slashes (/) are stripped off.
	 */
	public void testApplicationPath_TrimTrailingSlash() throws Exception {
		this.doApplicationPathTest("/path", "/CONTEXT/path/");
	}

	/**
	 * Ensure transforms to canonical path.
	 */
	public void testApplicationPath_CanonicalPath() throws Exception {
		this.doApplicationPathTest("/path", "//./CONTEXT//./path/../path");
	}

	/**
	 * Ensure invalid if parent path.
	 */
	public void testApplicationPath_ParentCanonicalPath() throws Exception {

		String contextPath = this.getContextPath();
		contextPath = (contextPath == null ? "" : contextPath);
		String requestUri = contextPath + "/..";
		try {
			// Transform to application path
			WebRouter.transformToApplicationCanonicalPath(requestUri, contextPath);
			fail("Should not be successful");

		} catch (BadRequestHttpException ex) {
			// Invalid request if no context path
			assertEquals("Invalid only if no context path", "", contextPath);
			assertEquals("Incorrect HTTP status", HttpStatus.BAD_REQUEST, ex.getHttpStatus());
			assertEquals("Incorrect message", "Invalid request URI path " + requestUri, ex.getEntity());

		} catch (InvalidContextPathException ex) {
			// Incorrect as missing context path
			assertTrue("Incorrect only if have context path", (contextPath.trim().length() > 0));
			assertEquals("Incorrect message",
					"Incorrect context path for application [context=" + contextPath + ", request=" + requestUri + "]",
					ex.getMessage());
		}
	}

	/**
	 * Ensure no segments canonical path results in root path.
	 */
	public void testApplicationPath_RootCanonicalPath() throws Exception {
		this.doApplicationPathTest("/", "/CONTEXT/path/..");
	}

	/**
	 * Ensure no segments canonical path results in root path.
	 */
	public void testApplicationPath_RootCanonicalPathWithTrailingSlash() throws Exception {
		this.doApplicationPathTest("/", "/CONTEXT/path/../");
	}

	/**
	 * Ensure with only protocol/domain that the root path is returned.
	 */
	public void testApplicationPath_RootPathWithOnlyProtocolAndDomainName() throws Exception {
		this.doApplicationPathTest("/", "http://www.officefloor.net/CONTEXT");
	}

	/**
	 * Ensure with domain and port that the root path is returned.
	 */
	public void testApplicationPath_RootPathWithDomainAndPort() throws Exception {
		this.doApplicationPathTest("/", "http://www.officefloor.net:7878/CONTEXT");
	}

	/**
	 * Ensure returns canonical path if starts with Protocol and Domain name.
	 */
	public void testApplicationPath_PathWithProtocolAndDomainName() throws Exception {
		this.doApplicationPathTest("/path", "https://www.officefloor.net/CONTEXT/path");
	}

	/**
	 * Ensure returns canonical path if starts with Domain and Port.
	 */
	public void testApplicationPath_PathWithDomainAndPort() throws Exception {
		this.doApplicationPathTest("/path", "https://www.officefloor.net:7979/CONTEXT/path");
	}

	/**
	 * Ensures parameters are not included with the path.
	 */
	public void testApplicationPath_PathWithParameters() throws Exception {
		this.doApplicationPathTest("/path", "/CONTEXT/path?name=value");
	}

	/**
	 * Ensures that with only parameters that returns root path.
	 */
	public void testApplicationPath_RootPathWithParametersOnly() throws Exception {
		this.doApplicationPathTest("/", "/CONTEXT?name=value");
	}

	/**
	 * Ensures fragment is not included with the path.
	 */
	public void testApplicationPath_PathWithFragment() throws Exception {
		this.doApplicationPathTest("/path", "/CONTEXT/path#fragment");
	}

	/**
	 * Ensures that with only fragment that returns root path.
	 */
	public void testApplicationPath_RootPathWithFragmentOnly() throws Exception {
		this.doApplicationPathTest("/", "/CONTEXT#fragment");
	}

	/**
	 * Ensures that can handle path ending with a '/'.
	 */
	public void testApplicationPath_DirectoryPathWithParameters() throws Exception {
		this.doApplicationPathTest("/path", "/CONTEXT/path/?name=value");
	}

	/**
	 * Ensure root path returned in root path followed by a fragment.
	 */
	public void testApplicationPath_RootPathWithFragment() throws Exception {
		this.doApplicationPathTest("/", "/CONTEXT/#fragment");
	}

	/**
	 * Undertakes the application path test.
	 * 
	 * @param expectedPath
	 *            Expected path.
	 * @param requestUri
	 *            {@link HttpRequest} request URI.
	 */
	private void doApplicationPathTest(String expectedPath, String requestUri) throws Exception {

		// Obtain the context path
		String contextPath = this.getContextPath();

		// Transform request URI for context
		if (requestUri != null) {
			contextPath = (contextPath == null ? "" : contextPath);
			requestUri = requestUri.replace("/CONTEXT", contextPath);
		}

		// Transform to application path
		String actualPath = WebRouter.transformToApplicationCanonicalPath(requestUri, contextPath);

		// Ensure correct path
		assertEquals("Incorrect application path", expectedPath, actualPath);
	}

	/**
	 * Ensure use unqualified link as both unsecured.
	 */
	public void testClientPath_UnsecureConnectionWithUnsecureLink() {
		this.doClientPathTest("/CONTEXT/path", "/CONTEXT/path", false, "/path", false);
	}

	/**
	 * Ensure use qualified link as require secure.
	 */
	public void testClientPath_UnsecureConnectionWithSecureLink() {
		this.doClientPathTest("https://host.officefloor.net/CONTEXT/path", "/CONTEXT/path", false, "/path", true);
	}

	/**
	 * Ensure use qualified link as require not secure.
	 */
	public void testClientPath_SecureConnectionWithUnsecureLink() {
		this.doClientPathTest("http://host.officefloor.net/CONTEXT/path", "/CONTEXT/path", true, "/path", false);
	}

	/**
	 * Ensure use unqualified link as both secure.
	 */
	public void testClientPath_SecureConnectionWithSecureLink() {
		this.doClientPathTest("/CONTEXT/path", "/CONTEXT/path", true, "/path", true);
	}

	/**
	 * Ensure provide HTTP port if not standard port.
	 */
	public void testClientPath_HttpPort() {
		this.doClientPathTest("http://host.officefloor.net:7878/CONTEXT/path", "/CONTEXT/path", true,
				"host.officefloor.net", 7878, 7979, "/path", false);
	}

	/**
	 * Ensure provide HTTPS port if not standard port.
	 */
	public void testClientPath_HttpsPort() {
		this.doClientPathTest("https://host.officefloor.net:7979/CONTEXT/path", "/CONTEXT/path", false,
				"host.officefloor.net", 7878, 7979, "/path", true);
	}

	/**
	 * Ensure provide appropriate unsecured client path for root.
	 */
	public void testClientPath_UnsecureRootPath() {
		this.doClientPathTest("http://host.officefloor.net/CONTEXT/", "/CONTEXT/", true, "/", false);
	}

	/**
	 * Ensure provide appropriate secured client path for root.
	 */
	public void testClientPath_SecureRoot() {
		this.doClientPathTest("https://host.officefloor.net/CONTEXT/", "/CONTEXT/", false, "/", true);
	}

	/**
	 * Undertakes the client path test with default details.
	 * 
	 * @param expectedUrl
	 *            Expected URL.
	 * @param expectedPath
	 *            Expected path.
	 * @param isSecureConnection
	 *            Indicates if the {@link ServerHttpConnection} is secure.
	 * @param applicationPath
	 *            Application path.
	 * @param isSecureLink
	 *            Indicates whether link is to be secure.
	 */
	private void doClientPathTest(String expectedUrl, String expectedPath, boolean isSecureConnection,
			String applicationPath, boolean isSecureLink) {
		this.doClientPathTest(expectedUrl, expectedPath, isSecureConnection, "host.officefloor.net", 80, 443,
				applicationPath, isSecureLink);
	}

	/**
	 * Undertakes the client path test.
	 * 
	 * @param expectedUrl
	 *            Expected URL.
	 * @param expectedPath
	 *            Expected path.
	 * @param isSecureConnection
	 *            Indicates if the {@link ServerHttpConnection} is secure.
	 * @param domain
	 *            Domain.
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @param applicationPath
	 *            Application path.
	 * @param isSecureLink
	 *            Indicates whether link is to be secure.
	 */
	private void doClientPathTest(String expectedUrl, String expectedPath, boolean isSecureConnection, String domain,
			int httpPort, int httpsPort, String applicationPath, boolean isSecureLink) {
		try {

			// Transform expected path for context
			String contextPath = this.getContextPath();
			contextPath = (contextPath == null ? "" : contextPath);
			expectedUrl = expectedUrl.replace("/CONTEXT", contextPath);
			expectedPath = expectedPath.replace("/CONTEXT", contextPath);

			// Record creating the URL
			final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);
			this.recordReturn(connection, connection.isSecure(), isSecureConnection);
			if (isSecureConnection != isSecureLink) {
				// Must obtain full path
				HttpServerLocation location = new HttpServerLocationImpl(domain, httpPort, httpsPort);
				this.recordReturn(connection, connection.getServerLocation(), location);
			}

			// Test
			this.replayMockObjects();
			HttpApplicationState application = new HttpApplicationStateManagedObjectSource(contextPath);
			String clientUrl = application.createApplicationClientUrl(isSecureLink, applicationPath, connection);
			String clientPath = application.createApplicationClientPath(applicationPath);
			this.verifyMockObjects();

			// Validate correct client path
			assertEquals("Incorrect client URL", expectedUrl, clientUrl);
			assertEquals("Incorrect client path", expectedPath, clientPath);

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

}