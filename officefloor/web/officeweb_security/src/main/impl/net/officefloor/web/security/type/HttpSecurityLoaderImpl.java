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
package net.officefloor.web.security.type;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.web.security.type.HttpSecurityDependencyType;
import net.officefloor.web.security.type.HttpSecurityFlowType;
import net.officefloor.web.security.type.HttpSecurityLoader;
import net.officefloor.web.security.type.HttpSecurityType;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceMetaData;

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
	public <A, AC, C, O extends Enum<O>, F extends Enum<F>> PropertyList loadSpecification(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource) {

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
	public <A, AC, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource, PropertyList propertyList) {

		// Create the adaptation over the security source
		HttpSecurityManagedObjectAdapterSource<O> adapter = new HttpSecurityManagedObjectAdapterSource<>(
				httpSecuritySource);

		// Load the managed object type
		ManagedObjectType<O> moType = this.loader.loadManagedObjectType(adapter, propertyList);
		if (moType == null) {
			return null; // failed to obtain type
		}

		// Obtain the meta-data
		@SuppressWarnings("unchecked")
		HttpSecuritySourceMetaData<A, AC, C, O, F> metaData = (HttpSecuritySourceMetaData<A, AC, C, O, F>) adapter
				.getHttpSecuritySourceMetaData();

		// Obtain the authentication type
		Class<A> authenticationType = metaData.getAuthenticationClass();

		// Obtain the credentials type
		Class<C> credentialsType = metaData.getCredentialsClass();

		// Return the adapted type
		return new ManagedObjectHttpSecurityType<A, AC, C, O, F>(moType, authenticationType, credentialsType);
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
		<O extends Enum<O>> ManagedObjectType<O> loadManagedObjectType(HttpSecurityManagedObjectAdapterSource<O> source,
				PropertyList properties);
	}

	/**
	 * {@link HttpSecurityType} adapted from the {@link ManagedObjectType}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private static class ManagedObjectHttpSecurityType<A, AC, C, O extends Enum<O>, F extends Enum<F>>
			implements HttpSecurityType<A, AC, C, O, F> {

		/**
		 * {@link ManagedObjectType}.
		 */
		private final ManagedObjectType<O> moType;

		/**
		 * Authentication type.
		 */
		private final Class<A> authenticationType;

		/**
		 * Credentials type.
		 */
		private final Class<C> credentialsType;

		/**
		 * Initiate.
		 * 
		 * @param moType
		 *            {@link ManagedObjectType}.
		 * @param authenticationType
		 *            Authentication type.
		 * @param credentialsType
		 *            Credentials type.
		 */
		private ManagedObjectHttpSecurityType(ManagedObjectType<O> moType, Class<A> authenticationType,
				Class<C> credentialsType) {
			this.moType = moType;
			this.authenticationType = authenticationType;
			this.credentialsType = credentialsType;
		}

		/*
		 * ================= HttpSecurityType =====================
		 */

		@Override
		public Class<A> getAuthenticationClass() {
			return this.authenticationType;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Class<AC> getAccessControlClass() {
			return (Class<AC>) this.moType.getObjectClass();
		}

		@Override
		public Class<C> getCredentialsClass() {
			return this.credentialsType;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public HttpSecurityDependencyType<O>[] getDependencyTypes() {
			return AdaptFactory.adaptArray(this.moType.getDependencyTypes(), HttpSecurityDependencyType.class,
					new AdaptFactory<HttpSecurityDependencyType, ManagedObjectDependencyType<O>>() {
						@Override
						public HttpSecurityDependencyType<O> createAdaptedObject(
								ManagedObjectDependencyType<O> delegate) {
							return new ManagedObjectHttpSecurityDependencyType<O>(delegate);
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
	private static class ManagedObjectHttpSecurityDependencyType<O extends Enum<O>>
			implements HttpSecurityDependencyType<O> {

		/**
		 * {@link ManagedObjectDependencyType}.
		 */
		private final ManagedObjectDependencyType<O> dependency;

		/**
		 * Initiate.
		 * 
		 * @param dependency
		 *            {@link ManagedObjectDependencyType}.
		 */
		public ManagedObjectHttpSecurityDependencyType(ManagedObjectDependencyType<O> dependency) {
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
		public O getKey() {
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