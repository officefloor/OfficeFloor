/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;

/**
 * Provides listing of common tests for the various states of the
 * {@link HttpApplicationLocationMangedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpApplicationLocationManagedObjectTestCase
		extends OfficeFrameTestCase {

	/**
	 * {@link HttpApplicationLocation}.
	 */
	private HttpApplicationLocation location;

	/**
	 * Creates the {@link HttpApplicationLocation} for test.
	 * 
	 * @return {@link HttpApplicationLocation} for test.
	 */
	protected abstract HttpApplicationLocation createHttpApplicationLocation();

	@Override
	protected void setUp() throws Exception {
		this.location = this.createHttpApplicationLocation();
	}

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
		this.doApplicationPathTest("/", this.getContextPath());
	}

	/**
	 * Ensure leading white space is stripped off.
	 */
	public void testApplicationPath_TrimLeadingWhiteSpace() throws Exception {
		this.doApplicationPathTest("/path", "\t " + this.getContextPath()
				+ "/path");
	}

	/**
	 * Ensure trailing white space is stripped off.
	 */
	public void testApplicationPath_TrimTrailingWhiteSpace() throws Exception {
		this.doApplicationPathTest("/path", this.getContextPath() + "/path \n");
	}

	/**
	 * Ensure the same path is returned if already canonical.
	 */
	public void testApplicationPath_SameCanonicalPath() throws Exception {
		this.doApplicationPathTest("/path", this.getContextPath() + "/path");
	}

	/**
	 * Ensure trailing slashes (/) are stripped off.
	 */
	public void testApplicationPath_TrimTrailingSlash() throws Exception {
		this.doApplicationPathTest("/path", this.getContextPath() + "/path/");
	}

	/**
	 * Ensure transforms to canonical path.
	 */
	public void testApplicationPath_CanonicalPath() throws Exception {
		this.doApplicationPathTest("/path", "//." + this.getContextPath()
				+ "//./path/../path");
	}

	/**
	 * Ensure invalid if parent path.
	 */
	public void testApplicationPath_ParentCanonicalPath() throws Exception {
		String contextPath = this.getContextPath();
		String requestUri = contextPath + "/..";
		try {
			this.doApplicationPathTest(null, requestUri);
			fail("Should not be successful");

		} catch (InvalidHttpRequestUriException ex) {
			// Invalid request if no context path
			assertEquals("Invalid only if no context path", "", contextPath);
			assertEquals("Incorrect HTTP status", HttpStatus.SC_BAD_REQUEST,
					ex.getHttpStatus());
			assertEquals("Incorrect message", "Invalid request URI path ["
					+ requestUri + "]", ex.getMessage());

		} catch (IncorrectHttpRequestContextPathException ex) {
			// Incorrect as missing context path
			assertTrue("Incorrect only if have context path", (contextPath
					.trim().length() > 0));
			assertEquals("Incorrect HTTP status", HttpStatus.SC_NOT_FOUND,
					ex.getHttpStatus());
			assertEquals("Incorrect message",
					"Incorrect context path for application [context="
							+ contextPath + ", request=" + requestUri + "]",
					ex.getMessage());
		}
	}

	/**
	 * Ensure no segments canonical path results in root path.
	 */
	public void testApplicationPath_RootCanonicalPath() throws Exception {
		this.doApplicationPathTest("/", this.getContextPath() + "/path/..");
	}

	/**
	 * Ensure no segments canonical path results in root path.
	 */
	public void testApplicationPath_RootCanonicalPathWithTrailingSlash()
			throws Exception {
		this.doApplicationPathTest("/", this.getContextPath() + "/path/../");
	}

	/**
	 * Ensure with only protocol/domain that the root path is returned.
	 */
	public void testApplicationPath_RootPathWithOnlyProtocolAndDomainName()
			throws Exception {
		this.doApplicationPathTest("/",
				"http://www.officefloor.net" + this.getContextPath());
	}

	/**
	 * Ensure with domain and port that the root path is returned.
	 */
	public void testApplicationPath_RootPathWithDomainAndPort()
			throws Exception {
		this.doApplicationPathTest("/", "http://www.officefloor.net:7878"
				+ this.getContextPath());
	}

	/**
	 * Ensure returns canonical path if starts with Protocol and Domain name.
	 */
	public void testApplicationPath_PathWithProtocolAndDomainName()
			throws Exception {
		this.doApplicationPathTest("/path", "https://www.officefloor.net"
				+ this.getContextPath() + "/path");
	}

	/**
	 * Ensure returns canonical path if starts with Domain and Port.
	 */
	public void testApplicationPath_PathWithDomainAndPort() throws Exception {
		this.doApplicationPathTest("/path", "https://www.officefloor.net:7979"
				+ this.getContextPath() + "/path");
	}

	/**
	 * Ensures parameters are not included with the path.
	 */
	public void testApplicationPath_PathWithParameters() throws Exception {
		this.doApplicationPathTest("/path", this.getContextPath()
				+ "/path?name=value");
	}

	/**
	 * Ensures that with only parameters that returns root path.
	 */
	public void testApplicationPath_RootPathWithParametersOnly()
			throws Exception {
		this.doApplicationPathTest("/", this.getContextPath() + "?name=value");
	}

	/**
	 * Ensures fragment is not included with the path.
	 */
	public void testApplicationPath_PathWithFragment() throws Exception {
		this.doApplicationPathTest("/path", this.getContextPath()
				+ "/path#fragment");
	}

	/**
	 * Ensures that with only fragment that returns root path.
	 */
	public void testApplicationPath_RootPathWithFragmentOnly() throws Exception {
		this.doApplicationPathTest("/", this.getContextPath() + "#fragment");
	}

	/**
	 * Ensures that can handle path ending with a '/'.
	 */
	public void testApplicationPath_DirectoryPathWithParameters()
			throws Exception {
		this.doApplicationPathTest("/path", this.getContextPath()
				+ "/path/?name=value");
	}

	/**
	 * Ensure root path returned in root path followed by a fragment.
	 */
	public void testApplicationPath_RootPathWithFragment() throws Exception {
		this.doApplicationPathTest("/", this.getContextPath() + "/#fragment");
	}

	/**
	 * Obtains the context path to prefix paths.
	 * 
	 * @return Context path.
	 */
	protected String getContextPath() {
		String contextPath = this.location.getContextPath();
		return (contextPath == null ? "" : contextPath);
	}

	/**
	 * Undertakes the application path test.
	 * 
	 * @param expectedPath
	 *            Expected path.
	 * @param requestUri
	 *            {@link HttpRequest} request URI.
	 */
	private void doApplicationPathTest(String expectedPath, String requestUri)
			throws Exception {

		// Transform to application path
		String actualPath = this.location
				.transformToApplicationCanonicalPath(requestUri);

		// Ensure correct path
		assertEquals("Incorrect application path", expectedPath, actualPath);
	}

}