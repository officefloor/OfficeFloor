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
package net.officefloor.plugin.servlet.container;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link ServletConfirmerTest}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletConfirmerTest extends OfficeFrameTestCase {

	/**
	 * {@link ServletConfirmer} to test.
	 */
	private final ServletConfirmer confirmer = new ServletConfirmer();

	/**
	 * {@link HttpServletRequest} for recording.
	 */
	private final HttpServletRequest request = this.confirmer
			.getHttpServletRequestRecorder();

	/**
	 * Validate the {@link HttpServletRequest#getMethod()} can be confirmed.
	 */
	public void testConfirmGetMethod() throws Exception {

		// Record the method
		this.request.getMethod();

		// Confirm the value
		Object method = this.confirmer.confirm(null);
		assertEquals("Incorrect method", "POST", method);
	}

	/**
	 * Validates the {@link HttpServletRequest#getRequestURI()}.
	 */
	public void testConfirmUri() throws Exception {
		this.request.getRequestURI();
		assertEquals("Incorrect URI", "/test", this.confirmer.confirm("test"));
	}

	/**
	 * Validates the date header.
	 */
	public void testConfirmDateHeader() throws Exception {
		this.confirmer.setProxyReturn(new Long(1));
		this.request.getDateHeader("date");
		System.out.println("Date: "
				+ this.confirmer.confirm(null, "date",
						"Sun, 06 Nov 1994 08:49:37 GMT"));
	}

}