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

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractAsyncManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link HttpAuthentication}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpAuthenticationManagedObjectSource
		extends
		AbstractAsyncManagedObjectSource<HttpAuthenticationManagedObjectSource.Dependencies, HttpAuthenticationManagedObjectSource.Flows> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_SESSION
	}

	/**
	 * Flow keys.
	 */
	public static enum Flows {
		AUTHENTICATE, CHALLENGE
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractAsyncManagedObjectSource<Indexed,None>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<Indexed,None>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, Flows> context)
			throws Exception {
		// TODO implement
		// AbstractAsyncManagedObjectSource<Indexed,None>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<Indexed,None>.loadMetaData");
	}

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		// TODO implement
		// ManagedObjectSource<Dependencies,Flows>.sourceManagedObject
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSource<Dependencies,Flows>.sourceManagedObject");
	}

}