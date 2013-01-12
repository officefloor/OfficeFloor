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

import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceProperty;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSourceSpecification;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectUser;

/**
 * Adapts the {@link HttpSecuritySource} to be a {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityManagedObjectAdapterSource implements
		ManagedObjectSource<Indexed, Indexed> {

	/**
	 * Obtains the {@link ManagedObjectSourceSpecification} for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 * @param managedObjectLoader
	 *            {@link ManagedObjectLoader}.
	 * @return {@link PropertyList} for the {@link HttpSecuritySource}.
	 */
	public static synchronized <S, C, D extends Enum<D>, F extends Enum<F>, HS extends HttpSecuritySource<S, C, D, F>> PropertyList loadSpecification(
			HS httpSecuritySource, ManagedObjectLoader managedObjectLoader) {

		// Load the properties of the specification
		PropertyList properties;
		try {
			specificationInstance = httpSecuritySource;
			properties = managedObjectLoader
					.loadSpecification(HttpSecurityManagedObjectAdapterSource.class);
		} finally {
			// Ensure clear instance
			specificationInstance = null;
		}

		// Return the properties
		return properties;
	}

	/**
	 * {@link HttpSecuritySource} to provide the specification.
	 */
	private static HttpSecuritySource<?, ?, ?, ?> specificationInstance = null;

	/*
	 * ==================== ManagedObjectSource ========================
	 */

	@Override
	public ManagedObjectSourceSpecification getSpecification() {

		// Obtain the HTTP security specification
		HttpSecuritySourceSpecification specification = specificationInstance
				.getSpecification();
		if (specification == null) {
			return null;
		}

		// Return the adapted specification
		return new HttpSecurityManagedObjectSourceSpecification(specification);
	}

	@Override
	public void init(ManagedObjectSourceContext<Indexed> context)
			throws Exception {
		// TODO implement ManagedObjectSource<D,F>.init
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSource<D,F>.init");
	}

	@Override
	public ManagedObjectSourceMetaData<Indexed, Indexed> getMetaData() {
		// TODO implement ManagedObjectSource<D,F>.getMetaData
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSource<D,F>.getMetaData");
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context)
			throws Exception {
		// TODO implement ManagedObjectSource<D,F>.start
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSource<D,F>.start");
	}

	@Override
	public void sourceManagedObject(ManagedObjectUser user) {
		// TODO implement ManagedObjectSource<D,F>.sourceManagedObject
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSource<D,F>.sourceManagedObject");
	}

	@Override
	public void stop() {
		// TODO implement ManagedObjectSource<D,F>.stop
		throw new UnsupportedOperationException(
				"TODO implement ManagedObjectSource<D,F>.stop");
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceSpecification}.
	 */
	private static class HttpSecurityManagedObjectSourceSpecification implements
			ManagedObjectSourceSpecification {

		/**
		 * {@link HttpSecuritySourceSpecification}.
		 */
		private final HttpSecuritySourceSpecification specification;

		/**
		 * Initiate.
		 * 
		 * @param specification
		 *            {@link HttpSecuritySourceSpecification}.
		 */
		public HttpSecurityManagedObjectSourceSpecification(
				HttpSecuritySourceSpecification specification) {
			this.specification = specification;
		}

		/*
		 * ================ ManagedObjectSourceSpecification =============
		 */

		@Override
		public ManagedObjectSourceProperty[] getProperties() {

			// Obtain the properties
			HttpSecuritySourceProperty[] securityProperties = this.specification
					.getProperties();
			if (securityProperties == null) {
				return null;
			}

			// Adapt the properties
			ManagedObjectSourceProperty[] moProperties = new ManagedObjectSourceProperty[securityProperties.length];
			for (int i = 0; i < moProperties.length; i++) {
				HttpSecuritySourceProperty property = securityProperties[i];
				if (property != null) {
					moProperties[i] = new HttpSecurityManagedObjectSourceProperty(
							property);
				}
			}

			// Return the adapted properties
			return moProperties;
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceProperty}.
	 */
	private static class HttpSecurityManagedObjectSourceProperty implements
			ManagedObjectSourceProperty {

		/**
		 * {@link HttpSecuritySourceProperty}.
		 */
		private final HttpSecuritySourceProperty property;

		/**
		 * Initiate.
		 * 
		 * @param property
		 *            {@link HttpSecuritySourceProperty}.
		 */
		public HttpSecurityManagedObjectSourceProperty(
				HttpSecuritySourceProperty property) {
			this.property = property;
		}

		/*
		 * ================= ManagedObjectSourceProperty ===================
		 */

		@Override
		public String getName() {
			return this.property.getName();
		}

		@Override
		public String getLabel() {
			return this.property.getLabel();
		}
	}

}