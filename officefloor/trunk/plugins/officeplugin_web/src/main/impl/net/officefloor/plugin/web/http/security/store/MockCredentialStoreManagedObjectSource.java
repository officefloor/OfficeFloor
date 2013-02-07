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
package net.officefloor.plugin.web.http.security.store;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.MockHttpSecuritySource;

/**
 * <p>
 * Provides mocking of a {@link CredentialStore} in a similar way
 * {@link MockHttpSecuritySource} works.
 * <p>
 * Using this store allows the application to be built with its appropriate
 * {@link HttpSecuritySource} authentication scheme. This mocks the backing
 * {@link CredentialStore} for development and tests environments to avoid user
 * management. This therefore avoids the need to change the application
 * behaviour between environments (same {@link HttpSecuritySource} but differing
 * configured {@link CredentialStore}).
 * 
 * @author Daniel Sagenschneider
 */
public class MockCredentialStoreManagedObjectSource extends
		AbstractManagedObjectSource<None, None> {

	/*
	 * ===================== ManagedObjectSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractAsyncManagedObjectSource<None,None>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<None,None>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context)
			throws Exception {
		// TODO implement
		// AbstractAsyncManagedObjectSource<None,None>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<None,None>.loadMetaData");
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// TODO implement
		// AbstractManagedObjectSource<None,None>.getManagedObject
		throw new UnsupportedOperationException(
				"TODO implement AbstractManagedObjectSource<None,None>.getManagedObject");
	}

}