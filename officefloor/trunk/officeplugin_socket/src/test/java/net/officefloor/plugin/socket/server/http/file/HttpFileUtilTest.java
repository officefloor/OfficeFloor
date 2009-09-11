/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.file;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link HttpFileUtil}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileUtilTest extends OfficeFrameTestCase {

	/**
	 * Ensure empty path is not canonical.
	 */
	public void testEmptyPath() {
		assertNull("Null path", HttpFileUtil.transformToCanonicalPath(null));
		assertNull("Empty path", HttpFileUtil.transformToCanonicalPath(""));
		assertNull("Blank path", HttpFileUtil.transformToCanonicalPath(" "));
	}

	/**
	 * Ensure leading/trailing white space is stripped off.
	 */
	public void testLeadingTrailingWhiteSpace() {
		assertEquals("Leading white space", "/path", HttpFileUtil
				.transformToCanonicalPath(" /path"));
		assertEquals("Trailing white space", "/path", HttpFileUtil
				.transformToCanonicalPath("/path "));
	}

	/**
	 * Ensure the same path is returned if already canonical.
	 */
	public void testSameCanonicalPath() {
		assertSame("/path", HttpFileUtil.transformToCanonicalPath("/path"));
	}

	/**
	 * Ensure transforms to canonical path.
	 */
	public void testCanonicalPath() {
		assertEquals("/path", HttpFileUtil
				.transformToCanonicalPath("//./path/../path"));
	}

	/**
	 * Ensure returns <code>null</code> if canonical path returns parent path.
	 */
	public void testParentCanonicalPath() {
		assertNull("Should not return canonical path if is parent path",
				HttpFileUtil.transformToCanonicalPath("/.."));
	}

	/**
	 * Ensure no segments canonical path results in root path.
	 */
	public void testRootCanonicalPath() {
		assertEquals("Without trailing /", "/", HttpFileUtil
				.transformToCanonicalPath("/path/.."));
		assertEquals("With trailing /", "/", HttpFileUtil
				.transformToCanonicalPath("/path/../"));
	}

	/**
	 * Ensure with only protocol/domain that the root path is returned.
	 */
	public void testRootPathWithOnlyProtocolAndDomainName() {
		assertEquals("/", HttpFileUtil
				.transformToCanonicalPath("http://www.officefloor.net"));
	}

	/**
	 * Ensure returns canonical path if starts with Protocol and Domain name.
	 */
	public void testPathWithProtocolAndDomainName() {
		assertEquals("/path", HttpFileUtil
				.transformToCanonicalPath("http://www.officefloor.net/path"));
	}

	/**
	 * Ensures parameters are not included with the path.
	 */
	public void testPathWithParameters() {
		assertEquals("/path", HttpFileUtil
				.transformToCanonicalPath("/path?name=value"));
	}

	/**
	 * Ensures that with only parameters that returns root path.
	 */
	public void testRootPathWithParametersOnly() {
		assertEquals("/", HttpFileUtil.transformToCanonicalPath("?name=value"));
	}

	/**
	 * Ensures fragment is not included with the path.
	 */
	public void testPathWithFragment() {
		assertEquals("/path", HttpFileUtil
				.transformToCanonicalPath("/path#fragment"));
	}

	/**
	 * Ensures that with only fragment that returns root path.
	 */
	public void testRootPathWithFragmentOnly() {
		assertEquals("/", HttpFileUtil.transformToCanonicalPath("#fragment"));
	}

	/**
	 * Ensures that can handle path ending with a '/'.
	 */
	public void testDirectoryPathWithParameters() {
		assertEquals("/path", HttpFileUtil
				.transformToCanonicalPath("/path/?name=value"));
	}

	/**
	 * Ensure root path returned in root path followed by a fragment.
	 */
	public void testRootPathWithFragment() {
		assertEquals("/", HttpFileUtil.transformToCanonicalPath("/#fragment"));
	}

}