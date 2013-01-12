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
import net.officefloor.frame.impl.construct.source.SourceContextImpl;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectDependencyMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExtensionInterfaceMetaData;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectFlowMetaData;
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
public class HttpSecurityManagedObjectAdapterSource<D extends Enum<D>>
		implements ManagedObjectSource<D, Indexed> {

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
	@SuppressWarnings("unchecked")
	public static <S, C, D extends Enum<D>, F extends Enum<F>> PropertyList loadSpecification(
			HttpSecuritySource<S, C, D, F> httpSecuritySource,
			ManagedObjectLoader managedObjectLoader) {

		// Make safe given that using static field
		synchronized (HttpSecurityManagedObjectAdapterSource.class) {

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
	}

	/**
	 * {@link HttpSecuritySource} to provide the specification.
	 */
	private static HttpSecuritySource<?, ?, ?, ?> specificationInstance = null;

	/**
	 * {@link HttpSecuritySource} to provide type/functionality.
	 */
	private final HttpSecuritySource<?, ?, D, ?> securitySource;

	/**
	 * Should only be used for loading specification.
	 * 
	 * @throws IllegalStateException
	 *             If not being loaded to obtain specification.
	 */
	public HttpSecurityManagedObjectAdapterSource()
			throws IllegalStateException {
		this.securitySource = null;
		synchronized (HttpSecurityManagedObjectAdapterSource.class) {
			if (specificationInstance == null) {
				throw new IllegalStateException(
						"May only use for loading specification");
			}
		}
	}

	/**
	 * Initiate.
	 * 
	 * @param httpSecuritySource
	 *            {@link HttpSecuritySource}.
	 */
	public HttpSecurityManagedObjectAdapterSource(
			HttpSecuritySource<?, ?, D, ?> httpSecuritySource) {
		this.securitySource = httpSecuritySource;
	}

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
		this.securitySource
				.init(new ManagedObjectHttpSecuritySourceContext<Indexed>(true,
						context));
	}

	@Override
	public ManagedObjectSourceMetaData<D, Indexed> getMetaData() {

		// Obtain the HTTP security meta-data
		HttpSecuritySourceMetaData<?, ?, D, ?> metaData = this.securitySource
				.getMetaData();
		if (metaData == null) {
			return null;
		}

		// Return the adapted meta-data
		return new HttpSecurityManagedObjectSourceMetaData<D, Indexed>(metaData);
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

	/**
	 * {@link HttpSecuritySourceContext} adapting the
	 * {@link ManagedObjectSourceContext}.
	 */
	private static class ManagedObjectHttpSecuritySourceContext<F extends Enum<F>>
			extends SourceContextImpl implements HttpSecuritySourceContext {

		/**
		 * Initiate.
		 * 
		 * @param isLoadingType
		 *            Indicates if loading type.
		 * @param context
		 *            {@link ManagedObjectSourceContext}.
		 */
		public ManagedObjectHttpSecuritySourceContext(boolean isLoadingType,
				ManagedObjectSourceContext<F> context) {
			super(isLoadingType, context, context);
		}
	}

	/**
	 * {@link HttpSecuritySource} {@link ManagedObjectSourceMetaData}.
	 */
	private static class HttpSecurityManagedObjectSourceMetaData<D extends Enum<D>, F extends Enum<F>>
			implements ManagedObjectSourceMetaData<D, F> {

		/**
		 * {@link HttpSecuritySourceMetaData}.
		 */
		private final HttpSecuritySourceMetaData<?, ?, D, ?> metaData;

		/**
		 * Initiate.
		 * 
		 * @param metaData
		 *            {@link HttpSecuritySourceMetaData}.
		 */
		public HttpSecurityManagedObjectSourceMetaData(
				HttpSecuritySourceMetaData<?, ?, D, ?> metaData) {
			this.metaData = metaData;
		}

		/*
		 * ================== ManagedObjectSourceMetaData ==================
		 */

		@Override
		public Class<? extends ManagedObject> getManagedObjectClass() {
			return ManagedObject.class;
		}

		@Override
		public Class<?> getObjectClass() {
			return this.metaData.getSecurityClass();
		}

		@Override
		public ManagedObjectDependencyMetaData<D>[] getDependencyMetaData() {
			// TODO implement
			// ManagedObjectSourceMetaData<D,F>.getDependencyMetaData
			throw new UnsupportedOperationException(
					"TODO implement ManagedObjectSourceMetaData<D,F>.getDependencyMetaData");
		}

		@Override
		public ManagedObjectFlowMetaData<F>[] getFlowMetaData() {
			// TODO implement ManagedObjectSourceMetaData<D,F>.getFlowMetaData
			throw new UnsupportedOperationException(
					"TODO implement ManagedObjectSourceMetaData<D,F>.getFlowMetaData");
		}

		@Override
		public ManagedObjectExtensionInterfaceMetaData<?>[] getExtensionInterfacesMetaData() {
			// No extension interfaces
			return null;
		}
	}

}