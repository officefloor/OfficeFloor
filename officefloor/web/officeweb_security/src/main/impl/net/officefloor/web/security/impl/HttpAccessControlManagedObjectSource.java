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

import java.io.Serializable;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.PrivateSource;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpAccessControlFactory;

/**
 * {@link ManagedObjectSource} for the {@link HttpAccessControl}.
 * 
 * @author Daniel Sagenschneider
 */
@PrivateSource
public class HttpAccessControlManagedObjectSource<AC extends Serializable>
		extends AbstractManagedObjectSource<HttpAccessControlManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		ACCESS_CONTROL
	}

	/**
	 * Custom access control type.
	 */
	private final Class<AC> accessControlType;

	/**
	 * {@link HttpAccessControlFactory}.
	 */
	private final HttpAccessControlFactory<AC> httpAccessControlFactory;

	/**
	 * Instantiate.
	 * 
	 * @param httpSecurityType
	 *            {@link HttpSecurityType}.
	 */
	public HttpAccessControlManagedObjectSource(HttpSecurityType<?, AC, ?, ?, ?> httpSecurityType) {
		this.accessControlType = httpSecurityType.getAccessControlType();
		this.httpAccessControlFactory = httpSecurityType.getHttpAccessControlFactory();
	}

	/*
	 * ====================== ManagedObjectSource =========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		context.setObjectClass(HttpAccessControl.class);
		context.setManagedObjectClass(HttpAccessControlManagedObject.class);
		context.addDependency(Dependencies.ACCESS_CONTROL, this.accessControlType);
		context.addManagedObjectExtension(HttpAccessControl.class,
				(managedObject) -> (HttpAccessControl) managedObject.getObject());
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new HttpAccessControlManagedObject();
	}

	/**
	 * {@link HttpAccessControl} {@link ManagedObject}.
	 */
	private class HttpAccessControlManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link HttpAccessControl}.
		 */
		private HttpAccessControl httpAccessControl;

		/*
		 * ====================== ManagedObject =====================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {

			// Obtain the custom access control
			@SuppressWarnings("unchecked")
			AC accessControl = (AC) registry.getObject(Dependencies.ACCESS_CONTROL);

			// Adapt the access control
			this.httpAccessControl = HttpAccessControlManagedObjectSource.this.httpAccessControlFactory
					.createHttpAccessControl(accessControl);
		}

		@Override
		public Object getObject() {
			return this.httpAccessControl;
		}
	}

}
