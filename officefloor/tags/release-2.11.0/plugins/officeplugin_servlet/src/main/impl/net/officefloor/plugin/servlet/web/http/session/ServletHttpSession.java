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

import java.io.Serializable;
import java.util.Iterator;

import javax.servlet.Servlet;

import net.officefloor.plugin.servlet.time.Clock;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.session.HttpSessionAdministration;
import net.officefloor.plugin.web.http.session.InvalidatedHttpSessionException;
import net.officefloor.plugin.web.http.session.StoringHttpSessionException;

/**
 * {@link HttpSession} implementation backed by {@link Servlet}
 * {@link javax.servlet.http.HttpSession}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletHttpSession implements HttpSession,
		HttpSessionAdministration {

	/**
	 * {@link javax.servlet.http.HttpSession}.
	 */
	private final javax.servlet.http.HttpSession session;

	/**
	 * {@link Clock}.
	 */
	private final Clock clock;

	/**
	 * Token name.
	 */
	private final String tokenName;

	/**
	 * Initiate.
	 * 
	 * @param session
	 *            {@link javax.servlet.http.HttpSession}.
	 * @param clock
	 *            {@link Clock}.
	 * @param tokenName
	 *            Token name.
	 */
	public ServletHttpSession(javax.servlet.http.HttpSession session,
			Clock clock, String tokenName) {
		this.session = session;
		this.clock = clock;
		this.tokenName = tokenName;
	}

	/*
	 * ====================== HttpSession ============================
	 */

	@Override
	public String getSessionId() throws InvalidatedHttpSessionException {
		return this.session.getId();
	}

	@Override
	public String getTokenName() {
		return this.tokenName;
	}

	@Override
	public boolean isNew() throws InvalidatedHttpSessionException {
		return this.session.isNew();
	}

	@Override
	public long getCreationTime() throws InvalidatedHttpSessionException {
		return this.session.getCreationTime();
	}

	@Override
	public long getExpireTime() throws InvalidatedHttpSessionException {
		long expireTime = this.clock.currentTimeMillis()
				+ (this.session.getMaxInactiveInterval() * 1000);
		return expireTime;
	}

	@Override
	public void setExpireTime(long expireTime)
			throws StoringHttpSessionException, InvalidatedHttpSessionException {
		long maxIntervalTimeMilli = expireTime - this.clock.currentTimeMillis();
		int maxIntervalTime = (int) (maxIntervalTimeMilli / 1000); // seconds
		this.session.setMaxInactiveInterval(maxIntervalTime);
	}

	@Override
	public Serializable getAttribute(String name)
			throws InvalidatedHttpSessionException {
		return (Serializable) this.session.getAttribute(name);
	}

	@Override
	public Iterator<String> getAttributeNames()
			throws InvalidatedHttpSessionException {
		return new EnumerationIterator<String>(this.session.getAttributeNames());
	}

	@Override
	public void setAttribute(String name, Serializable object)
			throws StoringHttpSessionException, InvalidatedHttpSessionException {
		this.session.setAttribute(name, object);
	}

	@Override
	public void removeAttribute(String name)
			throws StoringHttpSessionException, InvalidatedHttpSessionException {
		this.session.removeAttribute(name);
	}

	@Override
	public HttpSessionAdministration getHttpSessionAdministration() {
		return this;
	}

	/*
	 * ========================= HttpSessionAdministration =====================
	 */

	@Override
	public void invalidate(boolean isRequireNewSession) throws Throwable {
		this.session.invalidate();
	}

	@Override
	public boolean isOperationComplete() throws Throwable {
		return true; // always complete
	}

	@Override
	public void store() throws Throwable {
		// Container manages
	}

}