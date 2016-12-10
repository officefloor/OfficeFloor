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
package net.officefloor.plugin.servlet.web.http.session;

import java.util.Enumeration;
import java.util.Iterator;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link ServletHttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpSessionTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link javax.servlet.http.HttpSession}.
	 */
	private final javax.servlet.http.HttpSession servletSession = this
			.createMock(javax.servlet.http.HttpSession.class);

	/**
	 * Mock {@link Clock}.
	 */
	private final Clock clock = this.createMock(Clock.class);

	/**
	 * {@link ServletHttpSession} being tested.
	 */
	private final HttpSession webSession = new ServletHttpSession(
			this.servletSession, this.clock, "jsessionid");

	/**
	 * Session Id.
	 */
	public void testSessionId() {
		this.recordReturn(this.servletSession, this.servletSession.getId(),
				"SESSION");
		this.replayMockObjects();
		assertEquals("Incorrect session Id", "SESSION",
				this.webSession.getSessionId());
		this.verifyMockObjects();
	}

	/**
	 * Token name.
	 */
	public void testTokenName() {
		assertEquals("Incorrect token name", "jsessionid",
				this.webSession.getTokenName());
	}

	/**
	 * Is new.
	 */
	public void testIsNew() {
		this.recordReturn(this.servletSession, this.servletSession.isNew(),
				true);
		this.replayMockObjects();
		assertTrue("Should be new", this.webSession.isNew());
		this.verifyMockObjects();
	}

	/**
	 * Creation time.
	 */
	public void testCreationTime() {
		this.recordReturn(this.servletSession,
				this.servletSession.getCreationTime(), 100);
		this.replayMockObjects();
		assertEquals("Incorrect creation time", 100,
				this.webSession.getCreationTime());
		this.verifyMockObjects();
	}

	/**
	 * Expire time.
	 */
	public void testExpireTime() {

		// Record specifying expire time
		this.recordReturn(this.clock, this.clock.currentTimeMillis(), 50);
		this.servletSession.setMaxInactiveInterval(1); // time in seconds

		// Record obtaining expire time
		this.recordReturn(this.servletSession,
				this.servletSession.getMaxInactiveInterval(), 2); // seconds
		this.recordReturn(this.clock, this.clock.currentTimeMillis(), 200);

		// Test
		this.replayMockObjects();
		this.webSession.setExpireTime(1050);
		assertEquals("Incorrect expire time", 2200,
				this.webSession.getExpireTime());
		this.verifyMockObjects();
	}

	/**
	 * Attributes.
	 */
	@SuppressWarnings("unchecked")
	public void testAttribute() {

		final Enumeration<String> enumeration = this
				.createMock(Enumeration.class);

		// Record specifying attribute
		this.servletSession.setAttribute("NAME", "OBJECT");

		// Record obtaining attribute
		this.recordReturn(this.servletSession,
				this.servletSession.getAttribute("NAME"), "OBJECT");

		// Record obtaining attribute names
		this.recordReturn(this.servletSession,
				this.servletSession.getAttributeNames(), enumeration);
		this.recordReturn(enumeration, enumeration.hasMoreElements(), true);
		this.recordReturn(enumeration, enumeration.nextElement(), "NAME");
		this.recordReturn(enumeration, enumeration.hasMoreElements(), false);

		// Record removing attribute
		this.servletSession.removeAttribute("NAME");

		// Test
		this.replayMockObjects();
		this.webSession.setAttribute("NAME", "OBJECT");
		assertEquals("Incorrect attribute",
				this.webSession.getAttribute("NAME"), "OBJECT");
		Iterator<String> names = this.webSession.getAttributeNames();
		assertTrue("Should be a name", names.hasNext());
		assertEquals("Incorrect name", "NAME", names.next());
		assertFalse("Should be only one name", names.hasNext());
		this.webSession.removeAttribute("NAME");
		this.verifyMockObjects();
	}

	/**
	 * Invalidate.
	 */
	public void testInvalidate() throws Throwable {
		this.servletSession.invalidate();
		this.replayMockObjects();
		this.webSession.getHttpSessionAdministration().invalidate(true);
		assertTrue("Operation is always complete", this.webSession
				.getHttpSessionAdministration().isOperationComplete());
		this.webSession.getHttpSessionAdministration().store();
		this.verifyMockObjects();
	}

}