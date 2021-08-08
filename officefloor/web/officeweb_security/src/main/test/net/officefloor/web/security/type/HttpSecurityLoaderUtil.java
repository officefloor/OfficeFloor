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
import java.util.function.Consumer;

import org.junit.Assert;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.compile.test.properties.PropertyListUtil;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.util.InvokedProcessServicer;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.mock.MockWebApp;
import net.officefloor.web.security.HttpAuthentication;
import net.officefloor.web.security.impl.AuthenticationContextManagedObjectSource;
import net.officefloor.web.security.impl.FunctionAuthenticateContext;
import net.officefloor.web.security.impl.FunctionLogoutContext;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.spi.security.AuthenticationContext;
import net.officefloor.web.spi.security.HttpSecurity;
import net.officefloor.web.spi.security.HttpSecuritySource;
import net.officefloor.web.spi.security.HttpSecuritySourceSpecification;
import net.officefloor.web.state.HttpRequestState;

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
	 * @param <A>                     Authentication type.
	 * @param <AC>                    Access control type.
	 * @param <C>                     Credentials type.
	 * @param <O>                     Dependency keys type.
	 * @param <F>                     {@link Flow} keys type.
	 * @param <HS>                    {@link HttpSecuritySource} type.
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
		return new HttpSecurityTypeBuilderImpl<>(moTypeBuilder);
	}

	/**
	 * Validates the {@link HttpSecurityType} contained in the
	 * {@link HttpSecurityTypeBuilder} against the {@link HttpSecurityType} loaded
	 * from the {@link HttpSecuritySource}.
	 * 
	 * @param <A>                      Authentication type.
	 * @param <AC>                     Access control type.
	 * @param <C>                      Credentials type.
	 * @param <O>                      Dependency keys type.
	 * @param <F>                      {@link Flow} keys type.
	 * @param <HS>                     {@link HttpSecuritySource} type.
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
		final HttpSecurityTypeBuilderImpl<A, AC, C, O, F> builder = (HttpSecurityTypeBuilderImpl<A, AC, C, O, F>) expectedHttpSecurityType;

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
	 * @param <A>                     Authentication type.
	 * @param <AC>                    Access control type.
	 * @param <C>                     Credentials type.
	 * @param <O>                     Dependency keys type.
	 * @param <F>                     {@link Flow} keys type.
	 * @param <HS>                    {@link HttpSecuritySource} type.
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
	 * @param <A>                     Authentication type.
	 * @param <AC>                    Access control type.
	 * @param <C>                     Credentials type.
	 * @param <O>                     Dependency keys type.
	 * @param <F>                     {@link Flow} keys type.
	 * @param <HS>                    {@link HttpSecuritySource} type.
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
	 * @param <A>                     Authentication type.
	 * @param <AC>                    Access control type.
	 * @param <C>                     Credentials type.
	 * @param <O>                     Dependency keys type.
	 * @param <F>                     {@link Flow} keys type.
	 * @param <HS>                    {@link HttpSecuritySource} type.
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
	 * @param <A>                Authentication type.
	 * @param <AC>               Access control type.
	 * @param <C>                Credentials type.
	 * @param <O>                Dependency keys type.
	 * @param <F>                {@link Flow} keys type.
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
	 * Creates an {@link AuthenticationContext} for testing.
	 * 
	 * @param connection         {@link ServerHttpConnection}.
	 * @param security           {@link HttpSecurity}.
	 * @param handleAuthenticate Handles the authentication.
	 * @return {@link AuthenticationContext} for testing.
	 * @throws Exception If fails to create the {@link AuthenticationContext}.
	 */
	@SuppressWarnings("unchecked")
	public static <AC extends Serializable, C> AuthenticationContext<AC, C> createAuthenticationContext(
			ServerHttpConnection connection, HttpSecurity<?, AC, C, ?, ?> security,
			Consumer<FunctionAuthenticateContext<AC, C>> handleAuthenticate) throws Throwable {

		// Create the dependencies
		HttpSession session = MockWebApp.mockSession(connection);
		HttpRequestState requestState = MockWebApp.mockRequestState(connection);

		// Handle authentication
		InvokedProcessServicer authenticator = (processIndex, parameter, managedObject) -> {
			FunctionAuthenticateContext<AC, C> context = (FunctionAuthenticateContext<AC, C>) parameter;
			if (handleAuthenticate != null) {
				// Handle authentication
				handleAuthenticate.accept(context);
			} else {
				// No handler, so no authentication
				context.accessControlChange(null, null);
			}
		};

		// Handle logout
		InvokedProcessServicer logout = (processIndex, parameter, managedObject) -> {
			FunctionLogoutContext<AC> context = (FunctionLogoutContext<AC>) parameter;
			context.accessControlChange(null, null);
		};

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.registerInvokeProcessServicer(AuthenticationContextManagedObjectSource.Flows.AUTHENTICATE,
				authenticator);
		loader.registerInvokeProcessServicer(AuthenticationContextManagedObjectSource.Flows.LOGOUT, logout);
		AuthenticationContextManagedObjectSource<?, AC, C, ?, ?> mos = loader
				.loadManagedObjectSource(new AuthenticationContextManagedObjectSource<>("test", security));

		// Load the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(AuthenticationContextManagedObjectSource.Dependencies.SERVER_HTTP_CONNECTION, connection);
		user.mapDependency(AuthenticationContextManagedObjectSource.Dependencies.HTTP_SESSION, session);
		user.mapDependency(AuthenticationContextManagedObjectSource.Dependencies.HTTP_REQUEST_STATE, requestState);
		ManagedObject managedObject = user.sourceManagedObject(mos);

		// Return the authentication context
		return (AuthenticationContext<AC, C>) managedObject.getObject();
	}

	/**
	 * Undertakes authentication.
	 * 
	 * @param authentication {@link HttpAuthentication}.
	 * @param credentials    Credentials.
	 * @throws Throwable If fails to authenticate or times out authenticating.
	 */
	public static <C> void authenticate(HttpAuthentication<C> authentication, C credentials) throws Throwable {
		doAuthenticationAction((handler) -> {
			authentication.authenticate(credentials, (error) -> handler.accept(error));
		});
	}

	/**
	 * Undertakes logout.
	 * 
	 * @param authentication {@link HttpAuthentication}.
	 * @throws Throwable If fails to logout or times out.
	 */
	public static <C> void logout(HttpAuthentication<C> authentication) throws Throwable {
		doAuthenticationAction((handler) -> {
			authentication.logout((error) -> handler.accept(error));
		});
	}

	/**
	 * Undertakes the authentication action.
	 * 
	 * @param action Authentication action.
	 * @throws Throwable If action fails or times out.
	 */
	private static void doAuthenticationAction(Consumer<Consumer<Throwable>> action) throws Throwable {

		// Attempt authentication
		boolean[] isComplete = new boolean[] { false };
		Throwable[] failure = new Throwable[] { null };
		action.accept((error) -> {
			synchronized (isComplete) {
				isComplete[0] = true;
				failure[0] = error;
				isComplete.notify();
			}
		});

		// Wait for authentication
		long endTime = System.currentTimeMillis() + 3000;
		synchronized (isComplete) {
			while (!isComplete[0]) {

				// Determine if timed out
				if (endTime < System.currentTimeMillis()) {
					Assert.fail("Timed out waiting for authentication");
				}

				// Wait some time
				isComplete.wait(10);
			}

			// Determine if failure
			if (failure[0] != null) {
				throw failure[0];
			}
		}
	}

	/**
	 * All access via static methods.
	 */
	private HttpSecurityLoaderUtil() {
	}

	/**
	 * {@link HttpSecurityTypeBuilder} implementation.
	 */
	private static class HttpSecurityTypeBuilderImpl<A, AC extends Serializable, C, O extends Enum<O>, F extends Enum<F>>
			implements HttpSecurityTypeBuilder {

		/**
		 * {@link ManagedObjectTypeBuilder}.
		 */
		private final ManagedObjectTypeBuilder moTypeBuilder;

		/**
		 * Authentication {@link Class}.
		 */
		private Class<A> authenticationClass;

		/**
		 * Access Control {@link Class}.
		 */
		private Class<AC> accessControlClass;

		/**
		 * Credentials {@link Class}.
		 */
		private Class<C> credentialsClass;

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
		@SuppressWarnings("unchecked")
		public void setAuthenticationClass(Class<?> authenticationClass) {
			this.authenticationClass = (Class<A>) authenticationClass;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void setAccessControlClass(Class<?> accessControlClass) {
			this.accessControlClass = (Class<AC>) accessControlClass;
		}

		@Override
		@SuppressWarnings("unchecked")
		public void setCredentialsClass(Class<?> credentialsClass) {
			this.credentialsClass = (Class<C>) credentialsClass;
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

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <a, ac extends Serializable, c, o extends Enum<o>, f extends Enum<f>> HttpSecurityType<a, ac, c, o, f> build() {
			return new HttpSecurityTypeImpl(this.authenticationClass, null, this.accessControlClass, null,
					this.credentialsClass, this.moTypeBuilder.build(), null);
		}
	}

}
