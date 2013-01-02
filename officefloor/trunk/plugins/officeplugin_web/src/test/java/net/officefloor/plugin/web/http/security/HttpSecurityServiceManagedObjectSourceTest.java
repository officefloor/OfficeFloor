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

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.test.managedobject.ManagedObjectLoaderUtil;
import net.officefloor.compile.test.managedobject.ManagedObjectTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.frame.util.ManagedObjectSourceStandAlone;
import net.officefloor.frame.util.ManagedObjectUserStandAlone;
import net.officefloor.plugin.socket.server.http.HttpHeader;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.parse.impl.HttpRequestParserImpl;
import net.officefloor.plugin.web.http.security.HttpSecurity;
import net.officefloor.plugin.web.http.security.HttpSecurityService;
import net.officefloor.plugin.web.http.security.HttpSecurityServiceManagedObject;
import net.officefloor.plugin.web.http.security.HttpSecurityServiceManagedObjectSource;
import net.officefloor.plugin.web.http.security.scheme.AuthenticationException;
import net.officefloor.plugin.web.http.security.scheme.BasicHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySource;
import net.officefloor.plugin.web.http.security.scheme.HttpSecuritySourceContext;
import net.officefloor.plugin.web.http.security.scheme.BasicHttpSecuritySource.Dependencies;
import net.officefloor.plugin.web.http.security.store.CredentialEntry;
import net.officefloor.plugin.web.http.security.store.CredentialStore;
import net.officefloor.plugin.web.http.session.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

/**
 * Tests the {@link HttpSecurityServiceManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityServiceManagedObjectSourceTest extends
		OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		ManagedObjectLoaderUtil
				.validateSpecification(
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Authentication Scheme");
	}

	/**
	 * Ensure correct type for <code>None</code> authentication.
	 */
	public void testNoneType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						HttpSecurityServiceManagedObjectSource.NONE_AUTHENTICATION_SCHEME);
	}

	/**
	 * Ensure correct type for <code>Basic</code> authentication.
	 */
	public void testBasicType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();
		type.addDependency(
				"Dependency-" + Dependencies.CREDENTIAL_STORE.name(),
				CredentialStore.class, null, 2, null);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						HttpSecurityServiceManagedObjectSource.BASIC_AUTHENTICATION_SCHEME,
						BasicHttpSecuritySource.PROPERTY_REALM, "TestRealm");
	}

	/**
	 * Ensure correct type for <code>Digest</code> authentication.
	 */
	public void testDigestType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();
		type.addDependency(
				"Dependency-"
						+ net.officefloor.plugin.web.http.security.scheme.DigestHttpSecuritySource.Dependencies.CREDENTIAL_STORE
								.name(), CredentialStore.class, null, 2, null);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						HttpSecurityServiceManagedObjectSource.DIGEST_AUTHENTICATION_SCHEME,
						DigestHttpSecuritySource.PROPERTY_REALM, "TestRealm",
						DigestHttpSecuritySource.PROPERTY_PRIVATE_KEY,
						"TestPrivateKey");
	}

	/**
	 * Ensure can load other type with no dependencies.
	 */
	public void testNoDependenciesType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Negotiate",
						HttpSecurityServiceManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME,
						NoDependenciesSource.class.getName());
	}

	/**
	 * No dependencies.
	 */
	public static class NoDependenciesSource extends AbstractSource<None> {
		@Override
		public void init(HttpSecuritySourceContext<None> context)
				throws Exception {
			// No dependencies
		}
	}

	/**
	 * Ensure can load multiple dependencies.
	 */
	public void testMultipleDependenciesType() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();
		type.addDependency("Dependency-KEY_ONE", Integer.class, null, 2, null);
		type.addDependency("Dependency-KEY_TWO", Object.class, null, 3, null);

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Negotiate",
						HttpSecurityServiceManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME,
						MultipleDependenciesSource.class.getName());
	}

	/**
	 * Keys for multiple dependencies.
	 */
	public static enum MultipleDependencyKeys {
		KEY_ONE, KEY_TWO
	}

	/**
	 * Multiple dependencies.
	 */
	public static class MultipleDependenciesSource extends
			AbstractSource<MultipleDependencyKeys> {
		@Override
		public void init(
				HttpSecuritySourceContext<MultipleDependencyKeys> context)
				throws Exception {
			context.requireDependency(MultipleDependencyKeys.KEY_ONE,
					Integer.class);
			context.requireDependency(MultipleDependencyKeys.KEY_TWO,
					Object.class);
		}
	}

	/**
	 * Ensure fails if missing dependency for key.
	 */
	public void testMissingDependenciesType() {

		// Mock
		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Specify operation
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		ManagedObjectLoaderUtil.setNextOfficeFloorCompiler(compiler);

		// Record missing property
		issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				"Failed to init", null);
		this.control(issues).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				// Validate parameters
				for (int i = 0; i < 4; i++) {
					assertEquals("Incorrect parameter " + i, expected[i],
							actual[i]);
				}
				IllegalArgumentException ex = (IllegalArgumentException) actual[5];
				assertEquals("Incorrect failure",
						"Not all dependency keys registered", ex.getMessage());
				return true;
			}
		});

		// Load the type
		this.replayMockObjects();
		ManagedObjectType<Indexed> type = ManagedObjectLoaderUtil
				.loadManagedObjectType(
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Negotiate",
						HttpSecurityServiceManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME,
						MissingDependenciesSource.class.getName());
		this.verifyMockObjects();

		// Type should not be loaded
		assertNull("Type should not be loaded", type);
	}

	/**
	 * Missing dependencies.
	 */
	public static class MissingDependenciesSource extends
			AbstractSource<MultipleDependencyKeys> {
		@Override
		public void init(
				HttpSecuritySourceContext<MultipleDependencyKeys> context)
				throws Exception {
			context.requireDependency(MultipleDependencyKeys.KEY_ONE,
					Integer.class);
		}
	}

	/**
	 * Ensure throws failure if missing property.
	 */
	public void testMissingProperty() {

		// Mock
		final CompilerIssues issues = this.createMock(CompilerIssues.class);

		// Initiate expected values
		PropertySource.setup("property.name", "missing.value");

		// Specify operation
		OfficeFloorCompiler compiler = OfficeFloorCompiler
				.newOfficeFloorCompiler(null);
		compiler.setCompilerIssues(issues);
		ManagedObjectLoaderUtil.setNextOfficeFloorCompiler(compiler);

		// Record missing property
		issues.addIssue(null, null, AssetType.MANAGED_OBJECT, null,
				"Missing property 'property.name'");

		// Load the type
		this.replayMockObjects();
		ManagedObjectType<Indexed> type = ManagedObjectLoaderUtil
				.loadManagedObjectType(
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Negotiate",
						HttpSecurityServiceManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME,
						PropertySource.class.getName());
		this.verifyMockObjects();

		// Type should not be loaded
		assertNull("Type should not be loaded", type);
	}

	/**
	 * Ensure able to obtain property.
	 */
	public void testProperty() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();

		// Initiate expected values
		PropertySource.setup("property.name", "property.value");

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Negotiate",
						HttpSecurityServiceManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME,
						PropertySource.class.getName(), "property.name",
						"property.value");
	}

	/**
	 * Obtains property.
	 */
	public static class PropertySource extends AbstractSource<None> {

		/**
		 * Sets up for testing.
		 * 
		 * @param name
		 *            Property name.
		 * @param value
		 *            Expected property value.
		 */
		public static void setup(String name, String value) {
			propertyName = name;
			propertyValue = value;
		}

		private static String propertyName;

		private static String propertyValue;

		@Override
		public void init(HttpSecuritySourceContext<None> context)
				throws Exception {
			String value = context.getProperty(propertyName);
			assertEquals("Incorrect property value", propertyValue, value);
		}
	}

	/**
	 * Ensure able to obtain default value for property.
	 */
	public void testDefaultProperty() {

		// Create the expected type
		ManagedObjectTypeBuilder type = this.createCoreType();

		// Initiate expected values
		DefaultPropertySource.setup("property.name", "default.value");

		// Validate type
		ManagedObjectLoaderUtil
				.validateManagedObjectType(
						type,
						HttpSecurityServiceManagedObjectSource.class,
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						"Negotiate",
						HttpSecurityServiceManagedObjectSource.PROPERTY_HTTP_SECURITY_SOURCE_CLASS_NAME,
						DefaultPropertySource.class.getName());
	}

	/**
	 * Default property.
	 */
	public static class DefaultPropertySource extends AbstractSource<None> {

		/**
		 * Sets up for testing.
		 * 
		 * @param name
		 *            Property name.
		 * @param value
		 *            Default value to return from property.
		 */
		public static void setup(String name, String defaultValue) {
			propertyName = name;
			propertyDefaultValue = defaultValue;
		}

		private static String propertyName;

		private static String propertyDefaultValue;

		@Override
		public void init(HttpSecuritySourceContext<None> context)
				throws Exception {
			String value = context.getProperty(propertyName,
					propertyDefaultValue);
			assertEquals("Incorrect property default value",
					propertyDefaultValue, value);
		}
	}

	/**
	 * Ensure able to authenticate.
	 */
	public void testAuthenticate() throws Throwable {

		final Charset US_ASCII = HttpRequestParserImpl.US_ASCII;

		// Mock
		final ServerHttpConnection connection = this
				.createMock(ServerHttpConnection.class);
		final HttpSession session = this.createMock(HttpSession.class);
		final HttpRequest request = this.createMock(HttpRequest.class);
		final HttpHeader header = this.createMock(HttpHeader.class);
		final CredentialStore store = this.createMock(CredentialStore.class);
		final CredentialEntry entry = this.createMock(CredentialEntry.class);

		// Create the base 64 user name and password
		final String username = "Username";
		final String password = "Password";
		final byte[] passwordBytes = password.getBytes(US_ASCII);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		buffer.write(username.getBytes(US_ASCII));
		buffer.write(":".getBytes(US_ASCII));
		buffer.write(passwordBytes);
		byte[] encoded = Base64.encodeBase64(buffer.toByteArray());
		String base64UsernamePassword = new String(encoded, US_ASCII);

		// Record
		this.recordReturn(session, session.getAttribute("#HttpSecurity#"), null);
		this.recordReturn(connection, connection.getHttpRequest(), request);
		this.recordReturn(request, request.getHeaders(), Arrays.asList(header));
		this.recordReturn(header, header.getName(), "Authorization");
		this.recordReturn(header, header.getValue(), "Basic "
				+ base64UsernamePassword);
		this.recordReturn(store,
				store.retrieveCredentialEntry(username, "TestRealm"), entry);
		this.recordReturn(entry, entry.retrieveCredentials(), passwordBytes);
		this.recordReturn(store, store.getAlgorithm(), null);
		this.recordReturn(entry, entry.retrieveRoles(), Collections.EMPTY_SET);
		session.setAttribute("#HttpSecurity", null);
		this.control(session).setMatcher(new AlwaysMatcher());

		// Replay for testing
		this.replayMockObjects();

		// Load the managed object source
		ManagedObjectSourceStandAlone loader = new ManagedObjectSourceStandAlone();
		loader.addProperty(
				HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
				HttpSecurityServiceManagedObjectSource.BASIC_AUTHENTICATION_SCHEME);
		loader.addProperty(BasicHttpSecuritySource.PROPERTY_REALM, "TestRealm");
		HttpSecurityServiceManagedObjectSource source = loader
				.loadManagedObjectSource(HttpSecurityServiceManagedObjectSource.class);

		// Ensure correct managed object class
		Class<?> managedObjectClass = source.getMetaData()
				.getManagedObjectClass();
		assertEquals("Incorrect managed object class",
				HttpSecurityServiceManagedObject.class, managedObjectClass);

		// Load the managed object
		ManagedObjectUserStandAlone user = new ManagedObjectUserStandAlone();
		user.mapDependency(0, connection);
		user.mapDependency(1, session);
		user.mapDependency(2, store);
		ManagedObject managedObject = user.sourceManagedObject(source);

		// Obtain the service and authenticate
		HttpSecurityService service = (HttpSecurityService) managedObject
				.getObject();
		HttpSecurity security = service.authenticate();

		// Verify
		this.verifyMockObjects();

		// Ensure correct security
		assertEquals("Incorrect authentication", username,
				security.getRemoteUser());
	}

	/**
	 * Creates the {@link ManagedObjectTypeBuilder} as basis of type being
	 * loaded.
	 * 
	 * @return Initial core {@link ManagedObjectTypeBuilder}.
	 */
	private ManagedObjectTypeBuilder createCoreType() {
		// Create type with the template details
		ManagedObjectTypeBuilder type = ManagedObjectLoaderUtil
				.createManagedObjectTypeBuilder();
		type.setObjectClass(HttpSecurityService.class);
		type.addDependency("HTTP_CONNECTION", ServerHttpConnection.class, null,
				0, null);
		type.addDependency("HTTP_SESSION", HttpSession.class, null, 1, null);
		return type;
	}

	/**
	 * Abstract {@link HttpSecuritySource}.
	 */
	protected static abstract class AbstractSource<D extends Enum<D>>
			implements HttpSecuritySource<D> {

		/*
		 * ==================== HttpSecuritySource =======================
		 */

		@Override
		public String getAuthenticationScheme() {
			// Obtain the name based on class name
			String className = this.getClass().getSimpleName();
			String authenticationScheme = className.substring(0,
					(className.length() - "Source".length()));
			return authenticationScheme;
		}

		@Override
		public HttpSecurity authenticate(String parameters,
				ServerHttpConnection connection, HttpSession session,
				Map<D, Object> dependencies) throws AuthenticationException {
			fail("Should not authenticate for initialising");
			return null; // for compilation
		}

		@Override
		public void loadUnauthorised(ServerHttpConnection connection,
				HttpSession session, Map<D, Object> dependencies)
				throws AuthenticationException {
			fail("Should not authenticate for initialising");
		}
	}

}