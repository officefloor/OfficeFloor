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

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * <p>
 * {@link HttpAuthentication} that always provide anonymous authentication (
 * <code>null</code> HTTP Security).
 * <p>
 * This allows the {@link HttpAuthentication} to be available when not
 * explicitly configured. Note that the HTTP Security will always be
 * <code>null</code> and therefore anything requiring authentication may not be
 * accessible.
 * 
 * @author Daniel Sagenschneider
 */
public class AnonymousHttpAuthenticationManagedObjectSource<S, C> extends AbstractManagedObjectSource<None, None>
		implements ManagedObject, HttpAuthentication<S, C> {

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
	public void authenticate(HttpAuthenticateRequest<C> authenticationRequest) {
		// Complete immediately as always no security
		if (authenticationRequest != null) {
			authenticationRequest.authenticationComplete();
		}
	}

	@Override
	public S getHttpSecurity() throws IOException {
		// Anonymous so no security
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