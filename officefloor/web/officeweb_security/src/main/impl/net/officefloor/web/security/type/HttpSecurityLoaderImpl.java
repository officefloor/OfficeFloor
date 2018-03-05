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

import java.io.Serializable;

import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.web.security.HttpAccessControl;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.spi.security.HttpAccessControlFactory;
import net.officefloor.web.spi.security.HttpAuthenticationFactory;
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
	 * @param node
	 *            {@link Node} context for {@link CompilerIssues}.
	 * @param compilerIssues
	 *            {@link CompilerIssues}.
	 */
	public HttpSecurityLoaderImpl(ManagedObjectLoader managedObjectLoader, Node node, CompilerIssues compilerIssues) {
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

			@Override
			public CompileError addIssue(String issueDescription) {
				return compilerIssues.addIssue(node, issueDescription);
			}
		};
	}

	/**
	 * Initiate.
	 * 
	 * @param officeArchitect
	 *            {@link OfficeArchitect}.
	 * @param officeSourceContext
	 *            {@link OfficeFloorSourceContext}.
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource}.
	 */
	public HttpSecurityLoaderImpl(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext,
			final String managedObjectSourceName) {
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

			@Override
			public CompileError addIssue(String issueDescription) {
				return officeArchitect.addIssue(issueDescription);
			}
		};
	}

	/*
	 * ======================== HttpSecurityLoader ==========================
	 */

	@Override
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> PropertyList loadSpecification(
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
	@SuppressWarnings("unchecked")
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource, PropertyList propertyList) {

		// Create the adaptation over the security source
		HttpSecurityManagedObjectAdapterSource<O> adapter = new HttpSecurityManagedObjectAdapterSource<>(
				httpSecuritySource);

		// Load the managed object access control type
		ManagedObjectType<O> moAccessControlType = this.loader.loadManagedObjectType(adapter, propertyList);
		if (moAccessControlType == null) {
			return null; // failed to obtain type
		}

		// Obtain the meta-data
		HttpSecuritySourceMetaData<A, AC, C, O, F> metaData = (HttpSecuritySourceMetaData<A, AC, C, O, F>) adapter
				.getHttpSecuritySourceMetaData();

		// Obtain the authentication type and factory
		Class<A> authenticationType = metaData.getAuthenticationType();
		if (authenticationType == null) {
			this.loader.addIssue("No Authentication type provided");
			return null;
		}
		HttpAuthenticationFactory<A, C> httpAuthenticationFactory = null;
		if (!(HttpAuthentication.class.isAssignableFrom(authenticationType))) {
			httpAuthenticationFactory = metaData.getHttpAuthenticationFactory();
			if (httpAuthenticationFactory == null) {
				this.loader.addIssue("Must provide " + HttpAuthenticationFactory.class.getSimpleName()
						+ ", as Authentication does not implement " + HttpAuthentication.class.getSimpleName()
						+ " (Authentication Type: " + authenticationType.getName() + ")");
				return null; // must have factory
			}
		}

		// Obtain the access control type and factory
		Class<AC> accessControlType = metaData.getAccessControlType();
		if (accessControlType == null) {
			this.loader.addIssue("No Access Control type provided");
			return null;
		}
		HttpAccessControlFactory<AC> httpAccessControlFactory = null;
		if (!(HttpAccessControl.class.isAssignableFrom(accessControlType))) {
			httpAccessControlFactory = metaData.getHttpAccessControlFactory();
			if (httpAccessControlFactory == null) {
				this.loader.addIssue("Must provide " + HttpAccessControlFactory.class.getSimpleName()
						+ ", as Access Control does not implement " + HttpAccessControl.class.getSimpleName()
						+ " (Access Control Type: " + accessControlType.getName() + ")");
				return null;
			}
		}

		// Obtain the credentials type
		Class<C> credentialsType = metaData.getCredentialsType();

		// Return the adapted type
		return new HttpSecurityTypeImpl<A, AC, C, O, F>(authenticationType, httpAuthenticationFactory,
				accessControlType, httpAccessControlFactory, credentialsType, moAccessControlType);
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

		/**
		 * Adds a {@link CompilerIssue}.
		 * 
		 * @param issueDescription
		 *            Description of the issue.
		 * @return {@link CompileError}.
		 */
		CompileError addIssue(String issueDescription);
	}

	/**
	 * {@link HttpSecurityType} adapted from the {@link ManagedObjectType}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private static class HttpSecurityTypeImpl<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
			implements HttpSecurityType<A, AC, C, O, F> {

		/**
		 * Authentication type.
		 */
		private final Class<A> authenticationType;

		/**
		 * {@link HttpAuthenticationFactory}.
		 */
		private final HttpAuthenticationFactory<A, C> httpAuthenticationFactory;

		/**
		 * Access control type.
		 */
		private final Class<AC> accessControlType;

		/**
		 * {@link HttpAccessControlFactory}.
		 */
		private final HttpAccessControlFactory<AC> httpAccessControlFactory;

		/**
		 * Credentials type.
		 */
		private final Class<C> credentialsType;

		/**
		 * {@link ManagedObjectType}.
		 */
		private final ManagedObjectType<O> moAccessControlType;

		/**
		 * Initiate.
		 * 
		 * @param authenticationType
		 *            Authentication type.
		 * @param httpAuthenticationFactory
		 *            {@link HttpAccessControlFactory}.
		 * @param moAccessControlType
		 *            {@link ManagedObjectType}.
		 * @param httpAccessControlFactory
		 *            {@link HttpAccessControlFactory}.
		 * @param credentialsType
		 *            Credentials type.
		 */
		private HttpSecurityTypeImpl(Class<A> authenticationType,
				HttpAuthenticationFactory<A, C> httpAuthenticationFactory, Class<AC> accessControlType,
				HttpAccessControlFactory<AC> httpAccessControlFactory, Class<C> credentialsType,
				ManagedObjectType<O> moAccessControlType) {
			this.authenticationType = authenticationType;
			this.httpAuthenticationFactory = httpAuthenticationFactory;
			this.accessControlType = accessControlType;
			this.httpAccessControlFactory = httpAccessControlFactory;
			this.credentialsType = credentialsType;
			this.moAccessControlType = moAccessControlType;
		}

		/*
		 * ================= HttpSecurityType =====================
		 */

		@Override
		public Class<A> getAuthenticationType() {
			return this.authenticationType;
		}

		@Override
		public HttpAuthenticationFactory<A, C> getHttpAuthenticationFactory() {
			return this.httpAuthenticationFactory;
		}

		@Override
		public Class<AC> getAccessControlType() {
			return this.accessControlType;
		}

		@Override
		public HttpAccessControlFactory<AC> getHttpAccessControlFactory() {
			return this.httpAccessControlFactory;
		}

		@Override
		public Class<C> getCredentialsType() {
			return this.credentialsType;
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public HttpSecurityDependencyType<O>[] getDependencyTypes() {
			return AdaptFactory.adaptArray(this.moAccessControlType.getDependencyTypes(),
					HttpSecurityDependencyType.class,
					new AdaptFactory<HttpSecurityDependencyType, ManagedObjectDependencyType<O>>() {
						@Override
						public HttpSecurityDependencyType<O> createAdaptedObject(
								ManagedObjectDependencyType<O> delegate) {
							return new HttpSecurityDependencyTypeImpl<O>(delegate);
						}
					});
		}

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public HttpSecurityFlowType<?>[] getFlowTypes() {
			return AdaptFactory.adaptArray(this.moAccessControlType.getFlowTypes(), HttpSecurityFlowType.class,
					new AdaptFactory<HttpSecurityFlowType, ManagedObjectFlowType>() {
						@Override
						public HttpSecurityFlowType createAdaptedObject(ManagedObjectFlowType delegate) {
							return new HttpSecurityFlowTypeImpl<F>(delegate);
						}
					});
		}
	}

	/**
	 * {@link HttpSecurityDependencyType} adapted from the
	 * {@link ManagedObjectDependencyType}.
	 */
	private static class HttpSecurityDependencyTypeImpl<O extends Enum<O>> implements HttpSecurityDependencyType<O> {

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
		public HttpSecurityDependencyTypeImpl(ManagedObjectDependencyType<O> dependency) {
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
		public String getDependencyType() {
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
	 * {@link HttpSecurityFlowType} adapted from the {@link ManagedObjectFlowType}.
	 */
	private static class HttpSecurityFlowTypeImpl<F extends Enum<F>> implements HttpSecurityFlowType<F> {

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
		public HttpSecurityFlowTypeImpl(ManagedObjectFlowType<F> flow) {
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
		public String getArgumentType() {
			return this.flow.getArgumentType();
		}
	}

}