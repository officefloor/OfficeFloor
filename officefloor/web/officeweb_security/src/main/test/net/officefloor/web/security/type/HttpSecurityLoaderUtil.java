/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.security.type;

import java.io.Serializable;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;

/**
 * Utility class for testing the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityLoaderUtil {

	/**
	 * Validates the {@link HttpSecuritySourceSpecification} for the
	 * {@link HttpSecuritySource}.
	 * 
	 * @param                         <A> Authentication type.
	 * @param                         <AC> Access control type.
	 * @param                         <C> Credentials type.
	 * @param                         <O> Dependency keys type.
	 * @param                         <F> {@link Flow} keys type.
	 * @param                         <HS> {@link HttpSecuritySource} type.
	 * @param httpSecuritySourceClass {@link HttpSecuritySource} class.
	 * @param propertyNameLabels      Listing of name/label pairs for the
	 *                                {@link Property} instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, HS extends HttpSecuritySource<A, AC, C, O, F>> PropertyList validateSpecification(
			Class<HS> httpSecuritySourceClass, String... propertyNameLabels) {

		// Create an instance of the HTTP security source
		HS httpSecuritySource = newHttpSecuritySource(httpSecuritySourceClass);

		// Load the specification
		HttpSecurityLoader securityLoader = getHttpSecurityLoader(null);
		PropertyList propertyList = securityLoader.loadSpecification(httpSecuritySource);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList, propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link HttpSecurityTypeBuilder} to create the expected
	 * {@link HttpSecurityType}.
	 * 
	 * @return {@link HttpSecurityTypeBuilder}.
	 */
	public static HttpSecurityTypeBuilder createHttpSecurityTypeBuilder() {

		// Obtain the managed object type builder
		ManagedObjectTypeBuilder moTypeBuilder = ManagedObjectLoaderUtil.createManagedObjectTypeBuilder();

		// Return the HTTP security type builder
		return new HttpSecurityTypeBuilderImpl(moTypeBuilder);
	}

	/**
	 * Validates the {@link HttpSecurityType} contained in the
	 * {@link HttpSecurityTypeBuilder} against the {@link HttpSecurityType} loaded
	 * from the {@link HttpSecuritySource}.
	 * 
	 * @param                          <A> Authentication type.
	 * @param                          <AC> Access control type.
	 * @param                          <C> Credentials type.
	 * @param                          <O> Dependency keys type.
	 * @param                          <F> {@link Flow} keys type.
	 * @param                          <HS> {@link HttpSecuritySource} type.
	 * @param expectedHttpSecurityType {@link HttpSecurityTypeBuilder}.
	 * @param httpSecuritySourceClass  {@link HttpSecuritySource} class.
	 * @param propertyNameValues       {@link Property} name/value pairs.
	 * @return Validated {@link HttpSecurityType}.
	 */
	@SuppressWarnings("unchecked")
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, HS extends HttpSecuritySource<A, AC, C, O, F>> HttpSecurityType<A, AC, C, O, F> validateHttpSecurityType(
			HttpSecurityTypeBuilder expectedHttpSecurityType, Class<HS> httpSecuritySourceClass,
			final String... propertyNameValues) {

		// Cast to obtain expected HTTP security type
		if (!(expectedHttpSecurityType instanceof HttpSecurityTypeBuilderImpl)) {
			Assert.fail("builder must be created from createHttpSecurityTypeBuilder");
		}
		final HttpSecurityTypeBuilderImpl builder = (HttpSecurityTypeBuilderImpl) expectedHttpSecurityType;

		// Create an instance of the HTTP security source
		HS httpSecuritySource = newHttpSecuritySource(httpSecuritySourceClass);

		// Validate the managed object type information
		HttpSecurityManagedObjectAdapterSource.doOperation(httpSecuritySource, new Runnable() {
			@Override
			public void run() {
				ManagedObjectLoaderUtil.validateManagedObjectType(builder.moTypeBuilder,
						HttpSecurityManagedObjectAdapterSource.class, propertyNameValues);
			}
		});

		// Ensure correct credentials class
		HttpSecurityType<A, AC, C, O, F> securityType = loadHttpSecurityType(httpSecuritySource, propertyNameValues);
		Assert.assertEquals("Incorrect authentication class", builder.authenticationClass,
				securityType.getAuthenticationType());
		Assert.assertEquals("Incorrect access control class", builder.accessControlClass,
				securityType.getAccessControlType());
		Assert.assertEquals("Incorrect credentials class", builder.credentialsClass, securityType.getCredentialsType());

		// Return the HTTP security type
		return securityType;
	}

	/**
	 * Convenience method to load the {@link HttpSecuritySource} initialised ready
	 * for testing.
	 * 
	 * @param                         <A> Authentication type.
	 * @param                         <AC> Access control type.
	 * @param                         <C> Credentials type.
	 * @param                         <O> Dependency keys type.
	 * @param                         <F> {@link Flow} keys type.
	 * @param                         <HS> {@link HttpSecuritySource} type.
	 * @param httpSecuritySourceClass {@link HttpSecuritySource} class.
	 * @param propertyNameValues      {@link Property} name/value pairs to
	 *                                initialise the {@link HttpSecuritySource}.
	 * @return Initialised {@link HttpSecuritySource}.
	 */
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, HS extends HttpSecuritySource<A, AC, C, O, F>> HS loadHttpSecuritySource(
			Class<HS> httpSecuritySourceClass, String... propertyNameValues) {

		// Create an instance of HTTP security source
		HS httpSecuritySource = newHttpSecuritySource(httpSecuritySourceClass);

		// Load the HTTP security type to initialise the HTTP security source
		loadHttpSecurityType(httpSecuritySource, propertyNameValues);

		// Return the HTTP security source
		return httpSecuritySource;
	}

	/**
	 * Convenience method to load the {@link HttpSecurity} initialised ready for
	 * testing.
	 * 
	 * @param                         <A> Authentication type.
	 * @param                         <AC> Access control type.
	 * @param                         <C> Credentials type.
	 * @param                         <O> Dependency keys type.
	 * @param                         <F> {@link Flow} keys type.
	 * @param                         <HS> {@link HttpSecuritySource} type.
	 * @param httpSecuritySourceClass {@link HttpSecuritySource} class.
	 * @param propertyNameValues      {@link Property} name/value pairs to
	 *                                initialise the {@link HttpSecuritySource}.
	 * @return Initialised {@link HttpSecuritySource}.
	 */
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, HS extends HttpSecuritySource<A, AC, C, O, F>> HttpSecurity<A, AC, C, O, F> loadHttpSecurity(
			Class<HS> httpSecuritySourceClass, String... propertyNameValues) {

		// Load the HTTP security source
		HS httpSecuritySource = loadHttpSecuritySource(httpSecuritySourceClass, propertyNameValues);

		// Return the HTTP security
		return httpSecuritySource.sourceHttpSecurity(null);
	}

	/**
	 * Instantiates and instance of the {@link HttpSecuritySource}.
	 * 
	 * @param                         <A> Authentication type.
	 * @param                         <AC> Access control type.
	 * @param                         <C> Credentials type.
	 * @param                         <O> Dependency keys type.
	 * @param                         <F> {@link Flow} keys type.
	 * @param                         <HS> {@link HttpSecuritySource} type.
	 * @param httpSecuritySourceClass {@link HttpSecuritySource} class.
	 * @return New {@link HttpSecuritySource} instance.
	 */
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>, HS extends HttpSecuritySource<A, AC, C, O, F>> HS newHttpSecuritySource(
			Class<HS> httpSecuritySourceClass) {

		// Create an instance of the HTTP security source
		HS httpSecuritySource = null;
		try {
			httpSecuritySource = httpSecuritySourceClass.getDeclaredConstructor().newInstance();
		} catch (Exception ex) {
			Assert.fail("Failed to create instance of " + httpSecuritySourceClass.getName() + ": " + ex.getMessage()
					+ " [" + ex.getClass().getName() + "]");
		}

		// Return the instance
		return httpSecuritySource;
	}

	/**
	 * Loads the {@link HttpSecurityType}.
	 * 
	 * @param                    <A> Authentication type.
	 * @param                    <AC> Access control type.
	 * @param                    <C> Credentials type.
	 * @param                    <O> Dependency keys type.
	 * @param                    <F> {@link Flow} keys type.
	 * @param httpSecuritySource {@link HttpSecuritySource}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @return {@link HttpSecurityType}.
	 */
	public static <A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>> HttpSecurityType<A, AC, C, O, F> loadHttpSecurityType(
			HttpSecuritySource<A, AC, C, O, F> httpSecuritySource, String... propertyNameValues) {

		// Create the properties
		PropertyList propertyList = OfficeFloorCompiler.newPropertyList();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Load the HTTP security type
		HttpSecurityLoader securityLoader = getHttpSecurityLoader(null);
		HttpSecurityType<A, AC, C, O, F> securityType = securityLoader.loadHttpSecurityType(httpSecuritySource,
				propertyList);

		// Return the HTTP security type
		return securityType;
	}

	/**
	 * Obtains the {@link HttpSecurityLoader}.
	 * 
	 * @param classLoader {@link ClassLoader}. May be <code>null</code>.
	 * @return {@link HttpSecurityLoader}.
	 */
	private static HttpSecurityLoader getHttpSecurityLoader(ClassLoader classLoader) {
		OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(classLoader);
		CompilerIssues issues = new FailTestCompilerIssues();
		compiler.setCompilerIssues(issues);
		HttpSecurityLoader securityLoader = new HttpSecurityLoaderImpl(compiler);
		return securityLoader;
	}

	/**
	 * All access via static methods.
	 */
	private HttpSecurityLoaderUtil() {
	}

	/**
	 * {@link HttpSecurityTypeBuilder} implementation.
	 */
	private static class HttpSecurityTypeBuilderImpl implements HttpSecurityTypeBuilder {

		/**
		 * {@link ManagedObjectTypeBuilder}.
		 */
		private final ManagedObjectTypeBuilder moTypeBuilder;

		/**
		 * Authentication {@link Class}.
		 */
		private Class<?> authenticationClass;

		/**
		 * Access Control {@link Class}.
		 */
		private Class<?> accessControlClass;

		/**
		 * Credentials {@link Class}.
		 */
		private Class<?> credentialsClass;

		/**
		 * Initiate.
		 * 
		 * @param moTypeBuilder {@link ManagedObjectTypeBuilder}.
		 */
		public HttpSecurityTypeBuilderImpl(ManagedObjectTypeBuilder moTypeBuilder) {
			this.moTypeBuilder = moTypeBuilder;
			this.moTypeBuilder.setObjectClass(Void.class);
		}

		/*
		 * ================== HttpSecurityTypeBuilder ===================
		 */

		@Override
		public void setAuthenticationClass(Class<?> authenticationClass) {
			this.authenticationClass = authenticationClass;
		}

		@Override
		public void setAccessControlClass(Class<?> accessControlClass) {
			this.accessControlClass = accessControlClass;
		}

		@Override
		public void setCredentialsClass(Class<?> credentialsClass) {
			this.credentialsClass = credentialsClass;
		}

		@Override
		public void setInput(boolean isInput) {
			this.moTypeBuilder.setInput(isInput);
		}

		@Override
		public void addDependency(String name, Class<?> type, String typeQualifier, int index, Enum<?> key) {
			this.moTypeBuilder.addDependency(name, type, typeQualifier, index, key);
		}

		@Override
		public void addDependency(Enum<?> key, Class<?> type, String typeQualifier) {
			this.moTypeBuilder.addDependency(key, type, typeQualifier);
		}

		@Override
		public void addFlow(String name, Class<?> argumentType, int index, Enum<?> key) {
			this.moTypeBuilder.addFlow(name, argumentType, index, key);
		}

		@Override
		public void addFlow(Enum<?> key, Class<?> argumentType) {
			this.moTypeBuilder.addFlow(key, argumentType);
		}
	}

}