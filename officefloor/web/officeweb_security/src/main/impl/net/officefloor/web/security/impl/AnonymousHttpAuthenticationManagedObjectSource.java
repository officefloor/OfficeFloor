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
package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.server.http.HttpException;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpAuthenticateCallback;
import net.officefloor.web.spi.security.HttpLogoutRequest;

/**
 * <p>
 * {@link HttpAuthentication} that always provide anonymous authentication (
 * <code>null</code> {@link HttpAccessControl}).
 * <p>
 * This allows the {@link HttpAuthentication} to be available when not
 * explicitly configured. Note that the {@link HttpAccessControl} will always be
 * <code>null</code> and therefore anything requiring authentication may not be
 * accessible.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // HTTP Security added as required
public class AnonymousHttpAuthenticationManagedObjectSource<C> extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, HttpAuthentication<C> {

	/*
	 * ================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No properties required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(HttpAuthentication.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return this;
	}

	/*
	 * ====================== ManagedObject ============================
	 */

	@Override
	public Object getObject() throws Throwable {
		return this;
	}

	/*
	 * ==================== HttpAuthentication =========================
	 */

	@Override
	public boolean isAuthenticated() throws HttpException {
		return false; // anonymous so not authenticated
	}

	@Override
	public void authenticate(C credentials, HttpAuthenticateCallback authenticateCallback) {
		// Complete immediately as always no security
		if (authenticateCallback != null) {
			authenticateCallback.authenticationComplete();
		}
	}

	@Override
	public HttpAccessControl getAccessControl() {
		// Anonymous so no access control
		return null;
	}

	@Override
	public void logout(HttpLogoutRequest logoutRequest) {
		// Complete immediately as always no security
		if (logoutRequest != null) {
			logoutRequest.logoutComplete(null);
		}
	}

}