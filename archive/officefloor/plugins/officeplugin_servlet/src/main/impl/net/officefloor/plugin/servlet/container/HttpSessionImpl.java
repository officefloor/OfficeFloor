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
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSessionContext;

import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * {@link javax.servlet.http.HttpSession} implementation.
 * 
 * @author Daniel Sagenschneider
 */
@SuppressWarnings("deprecation")
public class HttpSessionImpl implements javax.servlet.http.HttpSession {

	/**
	 * {@link HttpSession}.
	 */
	private final HttpSession session;

	/**
	 * Last access time.
	 */
	private final long lastAccessTime;

	/**
	 * {@link Clock}.
	 */
	private final Clock clock;

	/**
	 * {@link ServletContext}.
	 */
	private final ServletContext context;

	/**
	 * Initiate.
	 * 
	 * @param session
	 *            {@link HttpSession}.
	 * @param lastAccessTime
	 *            Last access time.
	 * @param clock
	 *            {@link Clock}.
	 * @param context
	 *            {@link ServletContext}.
	 */
	public HttpSessionImpl(HttpSession session, long lastAccessTime,
			Clock clock, ServletContext context) {
		this.session = session;
		this.lastAccessTime = lastAccessTime;
		this.clock = clock;
		this.context = context;
	}

	/*
	 * ========================== HttpSession ============================
	 */

	@Override
	public long getCreationTime() {
		return this.session.getCreationTime();
	}

	@Override
	public String getId() {
		return this.session.getSessionId();
	}

	@Override
	public long getLastAccessedTime() {
		return this.lastAccessTime;
	}

	@Override
	public ServletContext getServletContext() {
		return this.context;
	}

	@Override
	public int getMaxInactiveInterval() {

		// Obtain the inactive interval in seconds
		long currentTime = this.clock.currentTimeMillis();
		long inactiveInterval = this.session.getExpireTime() - currentTime;
		inactiveInterval = (inactiveInterval == 0 ? 0 : inactiveInterval / 1000);

		// Return the max inactive interval
		return (int) inactiveInterval;
	}

	@Override
	public void setMaxInactiveInterval(int interval) {

		// Obtain expire time in milliseconds
		long expireTime;
		if (interval < 0) {
			// Never expire
			expireTime = Long.MAX_VALUE;
		} else {
			// Calculate expire time
			expireTime = this.clock.currentTimeMillis() + (interval * 1000);
		}

		// Specify the expire time
		this.session.setExpireTime(expireTime);
	}

	@Override
	public Object getAttribute(String name) {
		return this.session.getAttribute(name);
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Enumeration getAttributeNames() {
		return new IteratorEnumeration<String>(this.session.getAttributeNames());
	}

	@Override
	public void setAttribute(String name, Object value) {
		this.session.setAttribute(name, (Serializable) value);
	}

	@Override
	public void removeAttribute(String name) {
		this.session.removeAttribute(name);
	}

	@Override
	public void invalidate() {
		try {
			this.session.getHttpSessionAdministration().invalidate(false);
		} catch (Throwable ex) {
			throw new IllegalStateException("Failure in invalidating session",
					ex);
		}
	}

	@Override
	public boolean isNew() {
		return this.session.isNew();
	}

	/*
	 * ---------------------- deprecated methods ------------------------
	 */

	@Override
	public HttpSessionContext getSessionContext() {
		throw new UnsupportedOperationException(
				"HttpSession.getSessionContext deprecated as of version 2.1");
	}

	@Override
	public Object getValue(String name) {
		throw new UnsupportedOperationException(
				"HttpSession.getValue deprecated as of version 2.2");
	}

	@Override
	public String[] getValueNames() {
		throw new UnsupportedOperationException(
				"HttpSession.getValueNames deprecated as of version 2.2");
	}

	@Override
	public void putValue(String name, Object value) {
		throw new UnsupportedOperationException(
				"HttpSession.putValue deprecated as of version 2.2");
	}

	@Override
	public void removeValue(String name) {
		throw new UnsupportedOperationException(
				"HttpSession.removeValue deprecated as of version 2.2");
	}

}