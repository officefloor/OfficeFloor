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

package net.officefloor.web.security.type;

import java.io.Serializable;
import java.util.function.Supplier;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.OfficeFloorCompilerRunnable;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssue;
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
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * {@link HttpSecurityLoader} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityLoaderImpl implements HttpSecurityLoader, OfficeFloorCompilerRunnable<HttpSecurityLoader> {

	/**
	 * {@link Loader}.
	 */
	private final Loader loader;

	/**
	 * Instantiate for {@link OfficeFloorCompilerRunnable}.
	 */
	public HttpSecurityLoaderImpl() {
		this.loader = null;
	}

	/**
	 * Initiate.
	 * 
	 * @param compiler {@link OfficeFloorCompiler}.
	 */
	public HttpSecurityLoaderImpl(OfficeFloorCompiler compiler) {
		ManagedObjectLoader managedObjectLoader = compiler.getManagedObjectLoader();
		this.loader = new Loader() {

			@Override
			public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> S newInstance(
					Class<S> httpSecuritySourceClass) {
				return CompileUtil.newInstance(httpSecuritySourceClass, HttpSecuritySource.class, compiler,
						compiler.getCompilerIssues());
			}

			@Override
			@SuppressWarnings({ "rawtypes", "unchecked" })
			public PropertyList loadSpecification(
					Class<HttpSecurityManagedObjectAdapterSource> managedObjectSourceClass) {
				return managedObjectLoader.loadSpecification(managedObjectSourceClass);
			}

			@Override
			public <D extends Enum<D>> ManagedObjectType<D> loadManagedObjectType(ManagedObjectSource<D, ?> source,
					PropertyList properties) {
				return managedObjectLoader.loadManagedObjectType(source, properties);
			}

			@Override
			public Supplier<PropertyList> getPropertyListFactory() {
				return () -> compiler.createPropertyList();
			}

			@Override
			public CompileError addIssue(String issueDescription) {
				return compiler.getCompilerIssues().addIssue(compiler, issueDescription);
			}
		};
	}

	/**
	 * Initiate.
	 * 
	 * @param officeArchitect         {@link OfficeArchitect}.
	 * @param officeSourceContext     {@link OfficeFloorSourceContext}.
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 */
	public HttpSecurityLoaderImpl(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext,
			final String managedObjectSourceName) {
		this.loader = new Loader() {

			@Override
			public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> S newInstance(
					Class<S> httpSecuritySourceClass) {
				return CompileUtil.newInstance(httpSecuritySourceClass, HttpSecuritySource.class, officeArchitect);
			}

			@Override
			@SuppressWarnings("rawtypes")
			public PropertyList loadSpecification(
					Class<HttpSecurityManagedObjectAdapterSource> managedObjectSourceClass) {
				throw new IllegalStateException(
						"Should not require specification via " + OfficeFloorSourceContext.class.getName());
			}

			@Override
			@SuppressWarnings("unchecked")
			public <D extends Enum<D>> ManagedObjectType<D> loadManagedObjectType(ManagedObjectSource<D, ?> source,
					PropertyList properties) {
				return (ManagedObjectType<D>) officeSourceContext.loadManagedObjectType(managedObjectSourceName, source,
						properties);
			}

			@Override
			public Supplier<PropertyList> getPropertyListFactory() {
				return () -> officeSourceContext.createPropertyList();
			}

			@Override
			public CompileError addIssue(String issueDescription) {
				return officeArchitect.addIssue(issueDescription);
			}
		};
	}

	/*
	 * =================== OfficeFloorCompilerRunnable =====================
	 */

	@Override
	public HttpSecurityLoader run(OfficeFloorCompiler compiler, Object[] parameters) throws Exception {
		HttpSecurityLoader securityLoader = new HttpSecurityLoaderImpl(compiler);
		return securityLoader;

	}

	/*
	 * ======================== HttpSecurityLoader ==========================
	 */

	@Override
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> PropertyList loadSpecification(
			Class<S> httpSecuritySourceClass) {

		// Instantiate the security source
		HttpSecuritySource<A, AC, C, O, F> securitySource = this.loader.newInstance(httpSecuritySourceClass);
		if (securitySource == null) {
			return null; // failed to instantiate
		}

		// Return specification
		return this.loadSpecification(securitySource);
	}

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
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			Class<S> httpSecuritySourceClass, PropertyList propertyList) {

		// Instantiate the security source
		HttpSecuritySource<A, AC, C, O, F> securitySource = this.loader.newInstance(httpSecuritySourceClass);
		if (securitySource == null) {
			return null; // failed to instantiate
		}

		// Load and return type
		return this.loadHttpSecurityType(securitySource, propertyList);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource, PropertyList propertyList) {

		// Create the adaptation over the security source
		HttpSecurityManagedObjectAdapterSource<O> adapter = new HttpSecurityManagedObjectAdapterSource<>(
				httpSecuritySource, this.loader.getPropertyListFactory());

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

		// Load the supporting managed object types
		HttpSecuritySupportingManagedObjectImpl<?>[] supportingManagedObjects = adapter
				.getHttpSecuritySupportingManagedObjects();
		HttpSecuritySupportingManagedObjectType<?>[] supportingManagedObjectTypes = new HttpSecuritySupportingManagedObjectType[supportingManagedObjects.length];
		for (int i = 0; i < supportingManagedObjectTypes.length; i++) {
			HttpSecuritySupportingManagedObjectImpl<?> supportingManagedObject = supportingManagedObjects[i];

			// Load the supporting managed object type
			HttpSecuritySupportingManagedObjectType<?> supportingManagedObjectType = supportingManagedObject
					.loadHttpSecuritySupportingManagedObjectType(
							(supportingManagedObjectSource, supportingPropertyList) -> this.loader
									.loadManagedObjectType(supportingManagedObjectSource, supportingPropertyList));
			if (supportingManagedObjectType == null) {
				this.loader.addIssue("Failed to load " + HttpSecuritySupportingManagedObjectType.class.getSimpleName()
						+ " for " + HttpSecuritySupportingManagedObject.class.getSimpleName() + " "
						+ supportingManagedObject.getSupportingManagedObjectName());
				return null;
			}

			// Add the supporting managed object type
			supportingManagedObjectTypes[i] = supportingManagedObjectType;
		}

		// Return the adapted type
		return new HttpSecurityTypeImpl<A, AC, C, O, F>(authenticationType, httpAuthenticationFactory,
				accessControlType, httpAccessControlFactory, credentialsType, moAccessControlType,
				supportingManagedObjectTypes);
	}

	/**
	 * Loader.
	 */
	private static interface Loader {

		/**
		 * Creates a new instance of the {@link HttpSecuritySource}.
		 * 
		 * @param httpSecuritySourceClass {@link HttpSecuritySource} {@link Class}.
		 * @return New {@link HttpSecuritySource}.
		 */
		<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, S extends HttpSecuritySource<A, AC, C, O, F>> S newInstance(
				Class<S> httpSecuritySourceClass);

		/**
		 * Loads the specification.
		 * 
		 * @param managedObjectSourceClass {@link ManagedObjectSource} {@link Class}.
		 * @return {@link PropertyList} specification.
		 */
		@SuppressWarnings("rawtypes")
		PropertyList loadSpecification(Class<HttpSecurityManagedObjectAdapterSource> managedObjectSourceClass);

		/**
		 * Loads the {@link ManagedObjectType}.
		 * 
		 * @param source     {@link ManagedObjectSource}.
		 * @param properties {@link PropertyList}.
		 * @return {@link ManagedObjectType}.
		 */
		<O extends Enum<O>> ManagedObjectType<O> loadManagedObjectType(ManagedObjectSource<O, ?> source,
				PropertyList properties);

		/**
		 * Obtains the factory to create a {@link PropertyList}.
		 * 
		 * @return Factory to create a {@link PropertyList}.
		 */
		Supplier<PropertyList> getPropertyListFactory();

		/**
		 * Adds a {@link CompilerIssue}.
		 * 
		 * @param issueDescription Description of the issue.
		 * @return {@link CompileError}.
		 */
		CompileError addIssue(String issueDescription);
	}

}
