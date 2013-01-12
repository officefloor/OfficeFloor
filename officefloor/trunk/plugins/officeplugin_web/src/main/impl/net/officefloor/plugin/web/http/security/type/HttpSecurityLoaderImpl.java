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
package net.officefloor.plugin.web.http.security.type;

import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.plugin.web.http.security.HttpSecurityManagedObjectAdapterSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * {@link HttpSecurityLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityLoaderImpl implements HttpSecurityLoader {

	/**
	 * {@link ManagedObjectLoader}.
	 */
	private final ManagedObjectLoader managedObjectLoader;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectLoader
	 *            {@link ManagedObjectLoader}.
	 */
	public HttpSecurityLoaderImpl(ManagedObjectLoader managedObjectLoader) {
		this.managedObjectLoader = managedObjectLoader;
	}

	/*
	 * ======================== HttpSecurityLoader ==========================
	 */

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>> PropertyList loadSpecification(
			HttpSecuritySource<S, C, D, F> httpSecuritySource) {
		return HttpSecurityManagedObjectAdapterSource.loadSpecification(
				httpSecuritySource, this.managedObjectLoader);
	}

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>> HttpSecurityType<S, C, D, F> loadHttpSecurityType(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			PropertyList propertyList) {

		// Load the managed object type
		ManagedObjectType<D> moType = this.managedObjectLoader
				.loadManagedObjectType(
						new HttpSecurityManagedObjectAdapterSource<D>(
								httpSecuritySource), propertyList);
		if (moType == null) {
			return null; // failed to obtain type
		}

		// Return the adapted type
		return new ManagedObjectHttpSecurityType<S, C, D, F>(moType);
	}

	/**
	 * {@link HttpSecurityType} adapted from the {@link ManagedObjectType}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private static class ManagedObjectHttpSecurityType<S, C, D extends Enum<D>, F extends Enum<F>>
			implements HttpSecurityType<S, C, D, F> {

		/**
		 * {@link ManagedObjectType}.
		 */
		private final ManagedObjectType<D> moType;

		/**
		 * Initiate.
		 * 
		 * @param moType
		 *            {@link ManagedObjectType}.
		 */
		public ManagedObjectHttpSecurityType(ManagedObjectType<D> moType) {
			this.moType = moType;
		}

		/*
		 * ================= HttpSecurityType =====================
		 */

		@Override
		public Class<S> getSecurityClass() {
			return (Class<S>) this.moType.getObjectClass();
		}

		@Override
		public Class<C> getCredentialsClass() {
			// TODO implement HttpSecurityType<S,C,D,F>.getCredentialsClass
			throw new UnsupportedOperationException(
					"TODO implement HttpSecurityType<S,C,D,F>.getCredentialsClass");
		}

		@Override
		public HttpSecurityDependencyType<D>[] getDependencyTypes() {
			// TODO implement HttpSecurityType<S,C,D,F>.getDependencyTypes
			throw new UnsupportedOperationException(
					"TODO implement HttpSecurityType<S,C,D,F>.getDependencyTypes");
		}

		@Override
		public HttpSecurityFlowType<?>[] getFlowTypes() {
			// TODO implement HttpSecurityType<S,C,D,F>.getFlowTypes
			throw new UnsupportedOperationException(
					"TODO implement HttpSecurityType<S,C,D,F>.getFlowTypes");
		}
	}

}