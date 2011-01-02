/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.resource;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.protocol.HttpStatus;
import net.officefloor.plugin.web.http.resource.HttpResourceUtil;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;

/**
 * Test the {@link HttpResourceUtil}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResourceUtilTest extends OfficeFrameTestCase {

	/**
	 * Ensure null path is invalid.
	 */
	public void testNullPath() throws Exception {
		try {
			HttpResourceUtil.transformToCanonicalPath(null);
			fail("Should not be successful");
		} catch (InvalidHttpRequestUriException ex) {
			assertCause(HttpStatus.SC_BAD_REQUEST, null, ex);
		}
	}

	/**
	 * Ensure empty path is invalid.
	 */
	public void testEmptyPath() throws Exception {
		try {
			HttpResourceUtil.transformToCanonicalPath("");
			fail("Should not be successful");
		} catch (InvalidHttpRequestUriException ex) {
			assertCause(HttpStatus.SC_BAD_REQUEST, "", ex);
		}
	}

	/**
	 * Ensure blank path is invalid.
	 */
	public void testBlankPath() throws Exception {
		try {
			HttpResourceUtil.transformToCanonicalPath(" ");
			fail("Should not be successful");
		} catch (InvalidHttpRequestUriException ex) {
			assertCause(HttpStatus.SC_BAD_REQUEST, "", ex);
		}
	}

	/**
	 * Ensure leading/trailing white space is stripped off.
	 */
	public void testTrimWhiteSpace() throws Exception {
		assertEquals("Leading white space", "/path", HttpResourceUtil
				.transformToCanonicalPath("\t /path"));
		assertEquals("Trailing white space", "/path", HttpResourceUtil
				.transformToCanonicalPath("/path \n"));
	}

	/**
	 * Ensure the same path is returned if already canonical.
	 */
	public void testSameCanonicalPath() throws Exception {
		assertSame("/path", HttpResourceUtil.transformToCanonicalPath("/path"));
	}

	/**
	 * Ensure transforms to canonical path.
	 */
	public void testCanonicalPath() throws Exception {
		assertEquals("/path", HttpResourceUtil
				.transformToCanonicalPath("//./path/../path"));
	}

	/**
	 * Ensure invalid if parent path.
	 */
	public void testParentCanonicalPath() throws Exception {
		try {
			HttpResourceUtil.transformToCanonicalPath("/..");
			fail("Should not be successful");
		} catch (InvalidHttpRequestUriException ex) {
			assertCause(HttpStatus.SC_BAD_REQUEST, "/..", ex);
		}
	}

	/**
	 * Ensure no segments canonical path results in root path.
	 */
	public void testRootCanonicalPath() throws Exception {
		assertEquals("Without trailing /", "/", HttpResourceUtil
				.transformToCanonicalPath("/path/.."));
		assertEquals("With trailing /", "/", HttpResourceUtil
				.transformToCanonicalPath("/path/../"));
	}

	/**
	 * Ensure with only protocol/domain that the root path is returned.
	 */
	public void testRootPathWithOnlyProtocolAndDomainName() throws Exception {
		assertEquals("/", HttpResourceUtil
				.transformToCanonicalPath("http://www.officefloor.net"));
	}

	/**
	 * Ensure returns canonical path if starts with Protocol and Domain name.
	 */
	public void testPathWithProtocolAndDomainName() throws Exception {
		assertEquals("/path", HttpResourceUtil
				.transformToCanonicalPath("http://www.officefloor.net/path"));
	}

	/**
	 * Ensures parameters are not included with the path.
	 */
	public void testPathWithParameters() throws Exception {
		assertEquals("/path", HttpResourceUtil
				.transformToCanonicalPath("/path?name=value"));
	}

	/**
	 * Ensures that with only parameters that returns root path.
	 */
	public void testRootPathWithParametersOnly() throws Exception {
		assertEquals("/", HttpResourceUtil.transformToCanonicalPath("?name=value"));
	}

	/**
	 * Ensures fragment is not included with the path.
	 */
	public void testPathWithFragment() throws Exception {
		assertEquals("/path", HttpResourceUtil
				.transformToCanonicalPath("/path#fragment"));
	}

	/**
	 * Ensures that with only fragment that returns root path.
	 */
	public void testRootPathWithFragmentOnly() throws Exception {
		assertEquals("/", HttpResourceUtil.transformToCanonicalPath("#fragment"));
	}

	/**
	 * Ensures that can handle path ending with a '/'.
	 */
	public void testDirectoryPathWithParameters() throws Exception {
		assertEquals("/path", HttpResourceUtil
				.transformToCanonicalPath("/path/?name=value"));
	}

	/**
	 * Ensure root path returned in root path followed by a fragment.
	 */
	public void testRootPathWithFragment() throws Exception {
		assertEquals("/", HttpResourceUtil.transformToCanonicalPath("/#fragment"));
	}

	/**
	 * Asserts the cause is correct.
	 *
	 * @param expectedHttpStatus
	 *            Expected HTTP status.
	 * @param expectedPath
	 *            Expected path.
	 * @param actual
	 *            Actual {@link InvalidHttpRequestUriException}.
	 */
	private static void assertCause(int expectedHttpStatus,
			String expectedPath, InvalidHttpRequestUriException actual) {
		assertEquals("Incorrect HTTP status", expectedHttpStatus, actual
				.getHttpStatus());
		assertEquals("Incorrect message", "Invalid request URI path ["
				+ expectedPath + "]", actual.getMessage());
	}

}