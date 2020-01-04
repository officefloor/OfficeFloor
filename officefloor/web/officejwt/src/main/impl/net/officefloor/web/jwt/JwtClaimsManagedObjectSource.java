/*-
 * #%L
 * JWT Security
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
