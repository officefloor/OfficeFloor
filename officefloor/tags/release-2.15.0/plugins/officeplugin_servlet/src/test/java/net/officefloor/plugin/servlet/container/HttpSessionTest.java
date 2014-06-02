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
package net.officefloor.plugin.servlet.container;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Enumeration;

import javax.servlet.ServletContext;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionAdministration;

/**
 * Test the {@link HttpSessionImpl}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession delegate = this.createMock(HttpSession.class);

	/**
	 * Last access time for the {@link HttpRequest}.
	 */
	private final long lastAccessTime = 1000;

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext context = this
			.createMock(ServletContext.class);

	/**
	 * {@link Clock}.
	 */
	private final Clock clock = this.createMock(Clock.class);

	/**
	 * {@link HttpSessionImpl} to test.
	 */
	private final HttpSessionImpl session = new HttpSessionImpl(this.delegate,
			this.lastAccessTime, this.clock, this.context);

	/**
	 * Ensure able to obtain creation time.
	 */
	public void testCreationTime() {
		this.recordReturn(this.delegate, this.delegate.getCreationTime(), 1000);
		this.replayMockObjects();
		assertEquals("Incorrect creation time", 1000,
				this.session.getCreationTime());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain Id.
	 */
	public void testSessionId() {
		this.recordReturn(this.delegate, this.delegate.getSessionId(),
				"SessionId");
		this.replayMockObjects();
		assertEquals("Incorrect session id", "SessionId", this.session.getId());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to obtain last access time.
	 */
	public void testLastAccessTime() {
		this.replayMockObjects();
		assertEquals("Incorrect last access time", this.lastAccessTime,
				this.session.getLastAccessedTime());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to receive the {@link ServletContext}.
	 */
	public void testServletContext() {
		this.replayMockObjects();
		assertEquals("Incorrect servlet context", this.context,
				this.session.getServletContext());
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to configure max inactivity period.
	 */
	public void testMaxInactiveInterval() {
		final long currentTime = 1000;

		// Record specify expire time
		this.recordReturn(this.clock, this.clock.currentTimeMillis(),
				currentTime);
		this.delegate.setExpireTime(currentTime + (100 * 1000));

		// Record obtain expire time
		this.recordReturn(this.delegate, this.delegate.getExpireTime(),
				currentTime + (100 * 1000));
		this.recordReturn(this.clock, this.clock.currentTimeMillis(),
				currentTime);

		// Record never to expire
		this.delegate.setExpireTime(Long.MAX_VALUE); // never to expire

		this.replayMockObjects();
		this.session.setMaxInactiveInterval(100);
		assertEquals("Incorrect max inactive interval", 100,
				this.session.getMaxInactiveInterval());
		this.session.setMaxInactiveInterval(-1);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can use attributes.
	 */
	@SuppressWarnings("unchecked")
	public void testAttributes() {
		final Serializable attribute = this.createMock(Serializable.class);

		// Record setting, obtaining, removing an attribute
		this.delegate.setAttribute("attribute", attribute);
		this.recordReturn(this.delegate,
				this.delegate.getAttribute("attribute"), attribute);
		this.recordReturn(this.delegate, this.delegate.getAttributeNames(),
				Arrays.asList("attribute").iterator());
		this.delegate.removeAttribute("attribute");
		this.recordReturn(this.delegate,
				this.delegate.getAttribute("attribute"), null);

		// Test
		this.replayMockObjects();
		this.session.setAttribute("attribute", attribute);
		assertEquals("Incorrect attribute", attribute,
				this.session.getAttribute("attribute"));
		Enumeration<String> enumeration = this.session.getAttributeNames();
		assertTrue("Expecting an attribute name", enumeration.hasMoreElements());
		assertEquals("Incorrect attribute name", "attribute",
				enumeration.nextElement());
		assertFalse("Expecting only one attribute name",
				enumeration.hasMoreElements());
		this.session.removeAttribute("attribute");
		assertNull("Attribute should be removed",
				this.session.getAttribute("attribute"));
		this.verifyMockObjects();
	}

	/**
	 * Ensure can invalidate session.
	 */
	public void testInvalidate() throws Throwable {
		final HttpSessionAdministration admin = this
				.createMock(HttpSessionAdministration.class);
		this.recordReturn(this.delegate,
				this.delegate.getHttpSessionAdministration(), admin);
		admin.invalidate(false);
		this.replayMockObjects();
		this.session.invalidate();
		this.verifyMockObjects();
	}

	/**
	 * Ensure can determine if the session is new.
	 */
	public void testIsNew() {
		this.recordReturn(this.delegate, this.delegate.isNew(), true);
		this.replayMockObjects();
		assertTrue("Ensure identifies as new session", this.session.isNew());
		this.verifyMockObjects();
	}

}