/*-
 * #%L
 * JWT Security
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

package net.officefloor.web.jwt;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to provide the JWT claims.
 * 
 * @author Daniel Sagenschneider
 */
public class JwtClaimsManagedObjectSource
		extends AbstractManagedObjectSource<JwtClaimsManagedObjectSource.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		ACCESS_CONTROL
	}

	/**
	 * JWT claims {@link Class}.
	 */
	private final Class<?> claimsClass;

	/**
	 * Instantiate.
	 * 
	 * @param claimsClass JWT claims {@link Class}.
	 */
	public JwtClaimsManagedObjectSource(Class<?> claimsClass) {
		this.claimsClass = claimsClass;
	}

	/*
	 * =================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<Dependencies, None> context) throws Exception {
		context.setObjectClass(this.claimsClass);
		context.setManagedObjectClass(JwtClaimsManagedObject.class);
		context.addDependency(Dependencies.ACCESS_CONTROL, JwtHttpAccessControl.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new JwtClaimsManagedObject();
	}

	/**
	 * JWT claims {@link ManagedObject}.
	 */
	private static class JwtClaimsManagedObject implements CoordinatingManagedObject<Dependencies> {

		/**
		 * {@link JwtHttpAccessControl}.
		 */
		private JwtHttpAccessControl<?> accessControl;

		/*
		 * =================== ManagedObject ======================
		 */

		@Override
		public void loadObjects(ObjectRegistry<Dependencies> registry) throws Throwable {
			this.accessControl = (JwtHttpAccessControl<?>) registry.getObject(Dependencies.ACCESS_CONTROL);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.accessControl.getClaims();
		}
	}

}
