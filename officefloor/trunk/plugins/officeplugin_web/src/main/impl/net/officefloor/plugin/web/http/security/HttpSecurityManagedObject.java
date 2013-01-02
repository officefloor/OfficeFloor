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
package net.officefloor.plugin.web.http.security;

import java.io.IOException;

import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectSource.DependencyKeys;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;

/**
 * {@link ManagedObject} to provide a {@link HttpSecurity}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObject implements
		CoordinatingManagedObject<DependencyKeys>, AsynchronousManagedObject {

	/**
	 * {@link HttpSecurityManagedObjectSource}.
	 */
	private final HttpSecurityManagedObjectSource source;

	/**
	 * {@link AsynchronousListener}.
	 */
	private AsynchronousListener listener;

	/**
	 * {@link HttpSecurityService}.
	 */
	private HttpSecurityService service;

	/**
	 * {@link HttpSecurity}.
	 */
	private HttpSecurity security = null;

	/**
	 * Initiate.
	 * 
	 * @param source
	 *            {@link HttpSecurityManagedObjectSource}.
	 */
	public HttpSecurityManagedObject(HttpSecurityManagedObjectSource source) {
		this.source = source;
	}

	/**
	 * Invoked by the {@link HttpSecurityManagedObjectSource} to undertake the
	 * authentication.
	 * 
	 * @throws IOException
	 *             If fails to read authentication information.
	 * @throws AuthenticationException
	 *             If fails to authenticate.
	 */
	public void authenticate() throws IOException, AuthenticationException {
		try {

			// Attempt to authenticate
			HttpSecurity authenticatedSecurity = this.service.authenticate();

			// Load result of authentication
			synchronized (this) {
				this.security = authenticatedSecurity;
			}

		} finally {
			// Flagged completion of authentication
			this.listener.notifyComplete();
		}
	}

	/*
	 * ======================== ManagedObject ============================
	 */

	@Override
	public void registerAsynchronousCompletionListener(
			AsynchronousListener listener) {
		this.listener = listener;
	}

	@Override
	public void loadObjects(ObjectRegistry<DependencyKeys> registry)
			throws Throwable {

		// Obtain the HTTP Security Service
		this.service = (HttpSecurityService) registry
				.getObject(DependencyKeys.HTTP_SECURITY_SERVICE);

		// Trigger the authentication
		this.listener.notifyStarted();
		this.source.triggerAuthentication(this);
	}

	@Override
	public Object getObject() throws Throwable {
		synchronized (this) {
			return this.security;
		}
	}

}