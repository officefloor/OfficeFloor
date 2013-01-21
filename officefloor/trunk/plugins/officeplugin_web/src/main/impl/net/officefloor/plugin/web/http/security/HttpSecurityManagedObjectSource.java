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

import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.spi.managedobject.AsynchronousListener;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.spi.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.ObjectRegistry;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link HttpSecuritySource} (or its
 * equivalent application specific interface).
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectSource
		extends
		AbstractManagedObjectSource<HttpSecurityManagedObjectSource.Dependencies, None> {

	/**
	 * Name of {@link Property} for the HTTP security type.
	 */
	public static final String PROPERTY_HTTP_SECURITY_TYPE = "http.security.type";

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_AUTHENTICATION
	}

	/*
	 * ======================= ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO implement
		// AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadSpecification
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadSpecification");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context)
			throws Exception {
		// TODO implement
		// AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadMetaData
		throw new UnsupportedOperationException(
				"TODO implement AbstractAsyncManagedObjectSource<Indexed,Indexed>.loadMetaData");
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		// TODO implement
		// AbstractManagedObjectSource<Indexed,Indexed>.getManagedObject
		throw new UnsupportedOperationException(
				"TODO implement AbstractManagedObjectSource<Indexed,Indexed>.getManagedObject");
	}

	/**
	 * {@link ManagedObject} for the HTTP security.
	 */
	public static class HttpSecurityManagedObject implements
			AsynchronousManagedObject, CoordinatingManagedObject<Dependencies> {

		/*
		 * ==================== ManagedObject =========================
		 */

		@Override
		public void registerAsynchronousCompletionListener(
				AsynchronousListener listener) {
			// TODO implement
			// AsynchronousManagedObject.registerAsynchronousCompletionListener
			throw new UnsupportedOperationException(
					"TODO implement AsynchronousManagedObject.registerAsynchronousCompletionListener");
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry)
				throws Throwable {
			// TODO implement
			// CoordinatingManagedObject<Dependencies>.loadObjects
			throw new UnsupportedOperationException(
					"TODO implement CoordinatingManagedObject<Dependencies>.loadObjects");
		}

		@Override
		public Object getObject() throws Throwable {
			// TODO implement ManagedObject.getObject
			throw new UnsupportedOperationException(
					"TODO implement ManagedObject.getObject");
		}
	}

}