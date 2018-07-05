/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.impl;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.AsynchronousContext;
import net.officefloor.frame.api.managedobject.AsynchronousManagedObject;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;

/**
 * {@link ManagedObjectSource} for the default {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class DefaultHttpAccessControlManagedObjectSource
		extends AbstractManagedObjectSource<DefaultHttpAccessControlManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_AUTHENTICATION
	}

	/*
	 * ==================== ManagedObjectSource =======================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		context.setObjectClass(HttpAuthentication.class);
		context.setManagedObjectClass(DefaultHttpAccessControlManagedObject.class);
		context.addDependency(HttpAuthentication.class).setLabel(Dependencies.HTTP_AUTHENTICATION.name());
		context.addManagedObjectExtension(HttpAccessControl.class,
				(managedObject) -> (HttpAccessControl) managedObject.getObject());
	}

	@Override
	protected ManagedObject getManagedObject() {
		return new DefaultHttpAccessControlManagedObject();
	}

	/**
	 * {@link ManagedObjectSource} for the default {@link HttpAccessControl}.
	 */
	private static class DefaultHttpAccessControlManagedObject
			implements AsynchronousManagedObject, CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link AsynchronousContext}.
		 */
		private AsynchronousContext asynchronousContext;

		/**
		 * {@link HttpAuthentication}.
		 */
		private HttpAuthentication<?> httpAuthentication;

		/*
		 * ======================== ManagedObject ========================
		 */

		@Override
		public void setAsynchronousContext(AsynchronousContext context) {
			this.asynchronousContext = context;
		}

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the HTTP authentication
			this.httpAuthentication = (HttpAuthentication<?>) registry.getObject(Dependencies.HTTP_AUTHENTICATION);

			// Trigger authentication
			this.asynchronousContext.start(null);
			this.httpAuthentication.authenticate(null, (failure) -> {
				this.asynchronousContext.complete(null);
			});
		}

		@Override
		public Object getObject() throws Throwable {
			return this.httpAuthentication.getAccessControl();
		}
	}

}