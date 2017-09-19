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

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.web.http.security.HttpSecuritySource;

/**
 * {@link HttpSecurityLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityLoaderImpl implements HttpSecurityLoader {

	/**
	 * {@link Loader}.
	 */
	private final Loader loader;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectLoader
	 *            {@link ManagedObjectLoader}.
	 */
	public HttpSecurityLoaderImpl(final ManagedObjectLoader managedObjectLoader) {
		this.loader = new Loader() {
			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public PropertyList loadSpecification(
					Class<HttpSecurityManagedObjectAdapterSource> managedObjectSourceClass) {
				return managedObjectLoader.loadSpecification(managedObjectSourceClass);
			}

			@Override
			public <D extends Enum<D>> ManagedObjectType<D> loadManagedObjectType(
					HttpSecurityManagedObjectAdapterSource<D> source, PropertyList properties) {
				return managedObjectLoader.loadManagedObjectType(source, properties);
			}
		};
	}

	/**
	 * Initiate.
	 * 
	 * @param officeSourceContext
	 *            {@link OfficeFloorSourceContext}.
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 */
	public HttpSecurityLoaderImpl(final OfficeSourceContext officeSourceContext, final String managedObjectSourceName) {
		this.loader = new Loader() {
			@Override
			@SuppressWarnings("rawtypes")
			public PropertyList loadSpecification(
					Class<HttpSecurityManagedObjectAdapterSource> managedObjectSourceClass) {
				throw new IllegalStateException(
						"Should not require specification via " + OfficeFloorSourceContext.class.getName());
			}

			@Override
			@SuppressWarnings("unchecked")
			public <D extends Enum<D>> ManagedObjectType<D> loadManagedObjectType(
					HttpSecurityManagedObjectAdapterSource<D> source, PropertyList properties) {
				return (ManagedObjectType<D>) officeSourceContext.loadManagedObjectType(managedObjectSourceName, source,
						properties);
			}
		};
	}

	/*
	 * ======================== HttpSecurityLoader ==========================
	 */

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>> PropertyList loadSpecification(
			HttpSecuritySource<S, C, D, F> httpSecuritySource) {

		// Obtain the specification
		final PropertyList[] propertyList = new PropertyList[1];
		HttpSecurityManagedObjectAdapterSource.doOperation(httpSecuritySource, new Runnable() {
			@Override
			public void run() {
				// Obtain the specification
				propertyList[0] = HttpSecurityLoaderImpl.this.loader
						.loadSpecification(HttpSecurityManagedObjectAdapterSource.class);
			}
		});

		// Return the properties
		return propertyList[0];
	}

	@Override
	public <S, C, D extends Enum<D>, F extends Enum<F>> HttpSecurityType<S, C, D, F> loadHttpSecurityType(
			HttpSecuritySource<S, C, D, F> httpSecuritySource, PropertyList propertyList) {

		// Create the adaptation over the security source
		HttpSecurityManagedObjectAdapterSource<D> adapter = new HttpSecurityManagedObjectAdapterSource<D>(
				httpSecuritySource);

		// Load the managed object type
		ManagedObjectType<D> moType = this.loader.loadManagedObjectType(adapter, propertyList);
		if (moType == null) {
			return null; // failed to obtain type
		}

		// Obtain the credentials type
		@SuppressWarnings("unchecked")
		Class<C> credentialsType = (Class<C>) adapter.getHttpSecuritySourceMetaData().getCredentialsClass();

		// Return the adapted type
		return new ManagedObjectHttpSecurityType<S, C, D, F>(moType, credentialsType);
	}

	/**
	 * Loader.
	 */
	private static interface Loader {

		/**
		 * Loads the specification.
		 * 
		 * @param managedObjectSourceClass
		 *            {@link ManagedObjectSource} {@link Class}.
		 * @return {@link PropertyList} specification.
		 */
		@SuppressWarnings("rawtypes")
		PropertyList loadSpecification(Class<HttpSecurityManagedObjectAdapterSource> managedObjectSourceClass);

		/**
		 * Loads the {@link ManagedObjectType}.
		 * 
		 * @param source
		 *            {@link HttpSecurityManagedObjectAdapterSource}.
		 * @param properties
		 *            {@link PropertyList}.
		 * @return {@link ManagedObjectType}.
		 */
		<D extends Enum<D>> ManagedObjectType<D> loadManagedObjectType(HttpSecurityManagedObjectAdapterSource<D> source,
				PropertyList properties);
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
		 * Credentials type.
		 */
		private final Class<C> credentialsType;

		/**
		 * Initiate.
		 * 
		 * @param moType
		 *            {@link ManagedObjectType}.
		 * @param credentialsType
		 *            Credentials type.
		 */
		public ManagedObjectHttpSecurityType(ManagedObjectType<D> moType, Class<C> credentialsType) {
			this.moType = moType;
			this.credentialsType = credentialsType;
		}

		/*
		 * ================= HttpSecurityType =====================
		 */

		@Override
		@SuppressWarnings("unchecked")
		public Class<S> getSecurityClass() {
			return (Class<S>) this.moType.getObjectClass();
		}

		@Override
		public Class<C> getCredentialsClass() {
			return this.credentialsType;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public HttpSecurityDependencyType<D>[] getDependencyTypes() {
			return AdaptFactory.adaptArray(this.moType.getDependencyTypes(), HttpSecurityDependencyType.class,
					new AdaptFactory<HttpSecurityDependencyType, ManagedObjectDependencyType<D>>() {
						@Override
						public HttpSecurityDependencyType<D> createAdaptedObject(
								ManagedObjectDependencyType<D> delegate) {
							return new ManagedObjectHttpSecurityDependencyType<D>(delegate);
						}
					});
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public HttpSecurityFlowType<?>[] getFlowTypes() {
			return AdaptFactory.adaptArray(this.moType.getFlowTypes(), HttpSecurityFlowType.class,
					new AdaptFactory<HttpSecurityFlowType, ManagedObjectFlowType>() {
						@Override
						public HttpSecurityFlowType createAdaptedObject(ManagedObjectFlowType delegate) {
							return new ManagedObjectHttpSecurityFlowType<F>(delegate);
						}
					});
		}
	}

	/**
	 * {@link HttpSecurityDependencyType} adapted from the
	 * {@link ManagedObjectDependencyType}.
	 */
	private static class ManagedObjectHttpSecurityDependencyType<D extends Enum<D>>
			implements HttpSecurityDependencyType<D> {

		/**
		 * {@link ManagedObjectDependencyType}.
		 */
		private final ManagedObjectDependencyType<D> dependency;

		/**
		 * Initiate.
		 * 
		 * @param dependency
		 *            {@link ManagedObjectDependencyType}.
		 */
		public ManagedObjectHttpSecurityDependencyType(ManagedObjectDependencyType<D> dependency) {
			this.dependency = dependency;
		}

		/*
		 * ============= HttpSecurityDependencyType =========================
		 */

		@Override
		public String getDependencyName() {
			return this.dependency.getDependencyName();
		}

		@Override
		public int getIndex() {
			return this.dependency.getIndex();
		}

		@Override
		public Class<?> getDependencyType() {
			return this.dependency.getDependencyType();
		}

		@Override
		public String getTypeQualifier() {
			return this.dependency.getTypeQualifier();
		}

		@Override
		public D getKey() {
			return this.dependency.getKey();
		}
	}

	/**
	 * {@link HttpSecurityFlowType} adapted from the
	 * {@link ManagedObjectFlowType}.
	 */
	private static class ManagedObjectHttpSecurityFlowType<F extends Enum<F>> implements HttpSecurityFlowType<F> {

		/**
		 * {@link ManagedObjectFlowType}.
		 */
		private final ManagedObjectFlowType<F> flow;

		/**
		 * Initiate.
		 * 
		 * @param flow
		 *            {@link ManagedObjectFlowType}.
		 */
		public ManagedObjectHttpSecurityFlowType(ManagedObjectFlowType<F> flow) {
			this.flow = flow;
		}

		/*
		 * ==================== HttpSecurityFlowType =======================
		 */

		@Override
		public String getFlowName() {
			return this.flow.getFlowName();
		}

		@Override
		public F getKey() {
			return this.flow.getKey();
		}

		@Override
		public int getIndex() {
			return this.flow.getIndex();
		}

		@Override
		public Class<?> getArgumentType() {
			return this.flow.getArgumentType();
		}
	}

}