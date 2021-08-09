/*-
 * #%L
 * Web Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
