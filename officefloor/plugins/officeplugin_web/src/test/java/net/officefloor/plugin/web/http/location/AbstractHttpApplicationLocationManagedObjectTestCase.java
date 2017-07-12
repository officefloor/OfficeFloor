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
package net.officefloor.plugin.web.http.location;

import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationManagedObjectSource.Dependencies;

/**
 * Provides listing of common tests for the various states of the
 * {@link HttpApplicationLocationMangedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpApplicationLocationManagedObjectTestCase extends OfficeFrameTestCase {

	/**
	 * Creates the {@link HttpApplicationLocation} for test.
	 * 
	 * @param domain
	 *            Domain.
	 * @param httpPort
	 *            HTTP port.
	 * @param httpsPort
	 *            HTTPS port.
	 * @return {@link HttpApplicationLocation} for test.
	 */
	protected abstract HttpApplicationLocationMangedObject createHttpApplicationLocation(String domain, int httpPort,
			int httpsPort);

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

		// Create the application location
		HttpApplicationLocation location = this.createHttpApplicationLocation("host.officefloor.net", 80, 443);

		String contextPath = location.getContextPath();
		contextPath = (contextPath == null ? "" : contextPath);
		String requestUri = contextPath + "/..";
		try {
			// Transform to application path
			location.transformToApplicationCanonicalPath(requestUri);
			fail("Should not be successful");

		} catch (InvalidHttpRequestUriException ex) {
			// Invalid request if no context path
			assertEquals("Invalid only if no context path", "", contextPath);
			assertEquals("Incorrect HTTP status", HttpStatus.SC_BAD_REQUEST, ex.getHttpStatus());
			assertEquals("Incorrect message", "Invalid request URI path [" + requestUri + "]", ex.getMessage());

		} catch (IncorrectHttpRequestContextPathException ex) {
			// Incorrect as missing context path
			assertTrue("Incorrect only if have context path", (contextPath.trim().length() > 0));
			assertEquals("Incorrect HTTP status", HttpStatus.SC_NOT_FOUND, ex.getHttpStatus());
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
	 * Ensure use unqualified link as both unsecured.
	 */
	public void testClientPath_UnsecureConnectionWithUnsecureLink() {
		this.doClientPathTest("/CONTEXT/path", false, "/path", false);
	}

	/**
	 * Ensure use qualified link as require secure.
	 */
	public void testClientPath_UnsecureConnectionWithSecureLink() {
		this.doClientPathTest("https://host.officefloor.net/CONTEXT/path", false, "/path", true);
	}

	/**
	 * Ensure use qualified link as require not secure.
	 */
	public void testClientPath_SecureConnectionWithUnsecureLink() {
		this.doClientPathTest("http://host.officefloor.net/CONTEXT/path", true, "/path", false);
	}

	/**
	 * Ensure use unqualified link as both secure.
	 */
	public void testClientPath_SecureConnectionWithSecureLink() {
		this.doClientPathTest("/CONTEXT/path", true, "/path", true);
	}

	/**
	 * Ensure provide HTTP port if not standard port.
	 */
	public void testClientPath_HttpPort() {
		this.doClientPathTest("http://host.officefloor.net:7878/CONTEXT/path", true, "host.officefloor.net", 7878, 7979,
				"/path", false);
	}

	/**
	 * Ensure provide HTTPS port if not standard port.
	 */
	public void testClientPath_HttpsPort() {
		this.doClientPathTest("https://host.officefloor.net:7979/CONTEXT/path", false, "host.officefloor.net", 7878,
				7979, "/path", true);
	}

	/**
	 * Ensure provide appropriate unsecured client path for root.
	 */
	public void testClientPath_UnsecureRootPath() {
		this.doClientPathTest("http://host.officefloor.net/CONTEXT/", true, "/", false);
	}

	/**
	 * Ensure provide appropriate secured client path for root.
	 */
	public void testClientPath_SecureRoot() {
		this.doClientPathTest("https://host.officefloor.net/CONTEXT/", false, "/", true);
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

		// Create the application location
		HttpApplicationLocation location = this.createHttpApplicationLocation("host.officefloor.net", 80, 443);

		// Transform request URI for context
		if (requestUri != null) {
			String contextPath = location.getContextPath();
			contextPath = (contextPath == null ? "" : contextPath);
			requestUri = requestUri.replace("/CONTEXT", contextPath);
		}

		// Transform to application path
		String actualPath = location.transformToApplicationCanonicalPath(requestUri);

		// Ensure correct path
		assertEquals("Incorrect application path", expectedPath, actualPath);
	}

	/**
	 * Undertakes the client path test with default details.
	 * 
	 * @param expectedPath
	 *            Expected path.
	 * @param isSecureConnection
	 *            Indicates if the {@link ServerHttpConnection} is secure.
	 * @param applicationPath
	 *            Application path.
	 * @param isSecureLink
	 *            Indicates whether link is to be secure.
	 */
	private void doClientPathTest(String expectedPath, boolean isSecureConnection, String applicationPath,
			boolean isSecureLink) {
		this.doClientPathTest(expectedPath, isSecureConnection, "host.officefloor.net", 80, 443, applicationPath,
				isSecureLink);
	}

	/**
	 * Undertakes the client path test.
	 * 
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void doClientPathTest(String expectedPath, boolean isSecureConnection, String domain, int httpPort,
			int httpsPort, String applicationPath, boolean isSecureLink) {
		try {

			// Create the application location
			HttpApplicationLocationMangedObject location = this.createHttpApplicationLocation(domain, httpPort,
					httpsPort);

			final ObjectRegistry registry = this.createMock(ObjectRegistry.class);
			final ServerHttpConnection connection = this.createMock(ServerHttpConnection.class);

			// Record determine if over secure link
			this.recordReturn(registry, registry.getObject(Dependencies.SERVER_HTTP_CONNECTION), connection);
			this.recordReturn(connection, connection.isSecure(), isSecureConnection);

			// Test
			this.replayMockObjects();
			location.loadObjects(registry);
			String clientPath = location.transformToClientPath(applicationPath, isSecureLink);
			this.verifyMockObjects();

			// Transform expected path for context
			String contextPath = location.getContextPath();
			contextPath = (contextPath == null ? "" : contextPath);
			expectedPath = expectedPath.replace("/CONTEXT", contextPath);

			// Validate correct client path
			assertEquals("Incorrect client path", expectedPath, clientPath);

		} catch (Throwable ex) {
			throw fail(ex);
		}
	}

}