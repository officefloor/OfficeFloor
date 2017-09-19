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
package net.officefloor.plugin.web.http.parameters;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.easymock.AbstractMatcher;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoader;
import net.officefloor.plugin.web.http.parameters.HttpParametersLoaderImpl;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpServerTestUtil;

/**
 * Tests the {@link HttpParametersLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpParametersLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link MockInterface}.
	 */
	private final MockInterface object = this.createMock(MockInterface.class);

	/**
	 * Expected {@link MockRoleBean} instances.
	 */
	private final Map<String, MockRoleBean> roles = new HashMap<String, MockRoleBean>();

	/**
	 * Expected {@link MockProductBean} instances.
	 */
	private final Map<String, MockProductBean> products = new HashMap<String, MockProductBean>();

	/*
	 * ========================= Single value tests ========================
	 */

	/**
	 * Ensures all {@link Method} instances of the type are available.
	 */
	public void testAllMethodsAvailable() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.object.setDescription("description");
		// ignore should be ignored
		this
				.doTest(
						"GET",
						"/path?FirstName=Daniel&LastName=Sagenschneider&Description=description&Ignore=ignore",
						null, true);
	}

	/**
	 * Ensure able to load via alias mapping.
	 */
	public void testAliasMapping() throws Exception {
		this.object.setFirstName("Dan");
		this.doTest("GET", "/path?Alias=Dan", null, true, "Alias", "FirstName");
	}

	/**
	 * Ensure only the type properties are loaded.
	 */
	public void testOnlyTypeParametersLoaded() throws Exception {

		// Load the parameters
		HttpRequest request = HttpServerTestUtil
				.createHttpRequest(
						"GET",
						"/path?FirstName=Daniel&LastName=Sagenschneider&Description=description&Ignore=NotLoaded",
						null);
		HttpParametersLoader<MockInterface> loader = this.createLoader(true);
		MockObject mockObject = new MockObject();
		loader.loadParameters(request, mockObject);

		// Ensure only the type parameters are loaded
		assertEquals("Incorrect first name", "Daniel", mockObject.firstName);
		assertEquals("Incorrect last name", "Sagenschneider",
				mockObject.lastName);
		assertEquals("Incorrect description", "description",
				mockObject.description);
	}

	/**
	 * Ensure can ignore case in parameter names for loading.
	 */
	public void testCaseInsensitive() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.object.setDescription("description");
		this
				.doTest(
						"GET",
						"/path?firstname=Daniel&lastname=Sagenschneider&description=description&ignore=ignore",
						null, false);
	}

	/**
	 * Ensure can ignore case in parameter names for loading via an alias.
	 */
	public void testCaseInsensitiveAlias() throws Exception {
		this.object.setFirstName("Dan");
		this
				.doTest("GET", "/path?alias=Dan", null, false, "ALIAS",
						"firstname");
	}

	/**
	 * Ensure can load GET request with no parameters.
	 */
	public void testGetWithNoParameters() throws Exception {
		// Nothing loaded
		this.doTest("GET", "http://wwww.officefloor.net", null, true);
	}

	/**
	 * Ensure can not loads an unknown parameter.
	 */
	public void testGetWithUnknownParameter() throws Exception {
		// Nothing loaded
		this.doTest("GET", "/path?Unknown=NotLoad", null, true);
	}

	/**
	 * Ensure can load a single parameter.
	 */
	public void testGetWithOneParameter() throws Exception {
		this.object.setFirstName("Daniel");
		this.doTest("GET", "/path?FirstName=Daniel", null, true);
	}

	/**
	 * Ensure can load multiple parameters.
	 */
	public void testGetWithMultipleParameters() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("GET", "/path?FirstName=Daniel;LastName=Sagenschneider",
				null, true);
	}

	/**
	 * Ensures able to parse GET with only a segment.
	 */
	public void testGetWithFragment() throws Exception {
		// Nothing loaded
		this.doTest("GET", "/path#fragment", null, true);
	}

	/**
	 * Ensures able to parse GET with parameters and fragments.
	 */
	public void testGetWithParametersAndFragments() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("GET",
				"/path?FirstName=Daniel&LastName=Sagenschneider#fragment",
				null, true);
	}

	/**
	 * Ensure able to load parameter with <code>+</code> for space.
	 */
	public void testGetParameterWithSpace() throws Exception {
		this.object.setFirstName("Daniel Aaron");
		this.doTest("GET", "/path?FirstName=Daniel+Aaron", null, true);
	}

	/**
	 * Ensure able to load parameter with <code>%HH</code> escaping.
	 */
	public void testGetParameterWithEscape() throws Exception {
		this.object.setFirstName("Daniel Aaron");
		this.doTest("GET", "/path?FirstName=Daniel%20Aaron", null, true);
	}

	/**
	 * Ensure can load POST request with no parameters.
	 */
	public void testPostNoParameter() throws Exception {
		// Nothing loaded
		this.doTest("POST", "/path", "", true);
	}

	/**
	 * Ensure can load POST request with a parameter.
	 */
	public void testPostWithOneParameter() throws Exception {
		this.object.setFirstName("Daniel");
		this.doTest("POST", "/path", "FirstName=Daniel", true);
	}

	/**
	 * Ensure can load POST request with multiple parameters.
	 */
	public void testPostWithMultipleParameters() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("POST", "/path",
				"FirstName=Daniel&LastName=Sagenschneider", true);
	}

	/**
	 * Ensure can load POST request with parameters in the URI and the body.
	 */
	public void testPostWithUriAndBodyParameters() throws Exception {
		this.object.setFirstName("Daniel");
		this.object.setLastName("Sagenschneider");
		this.doTest("POST", "/path?FirstName=Daniel",
				"LastName=Sagenschneider", true);
	}

	/*
	 * ===================== Keyed value tests ========================
	 */

	/**
	 * Ensure able to load multiple values for a GET request. In other words
	 * will look for prefix on parameter name and use remaining of parameter
	 * name as key identifying the value.
	 */
	public void testGetWithMultipleValues() throws Exception {
		this.object.setMultipleValues("One", "1");
		this.object.setMultipleValues("Two", "2");
		this.doTest("GET", "/path?MultipleValues{One}=1&MultipleValues{Two}=2",
				null, true);
	}

	/**
	 * Ensure can load multiple values for a POST request.
	 */
	public void testPostWithMultipleValues() throws Exception {
		this.object.setMultipleValues("One", "1");
		this.object.setMultipleValues("Two", "2");
		this.doTest("POST", "/path",
				"MultipleValues{One}=1&MultipleValues{Two}=2", true);
	}

	/*
	 * ========================= Map loading tests ========================
	 */

	/**
	 * Ensure can map a property.
	 */
	public void testMapProperty() throws Exception {
		this.expectRole("key", "VALUE", null);
		this.doTest("GET", "/path?Roles{key}.RoleName=VALUE", null, true);
	}

	/**
	 * Ensure can load multiple properties on a mapped bean.
	 */
	public void testMapProperties() throws Exception {
		this.expectRole("key", "ROLE", "DESCRIPTION");
		this
				.doTest(
						"GET",
						"/path?roles{key}.roleName=ROLE&roles{key}.description=DESCRIPTION",
						null, false);
	}

	/**
	 * <p>
	 * Ensure can load multiple map entries.
	 * <p>
	 * Also shows how to create an indexed list.
	 */
	public void testMultipleMapEntries() throws Exception {
		this.expectRole("1", "ROLE1", "DESCRIPTION1");
		this.expectRole("2", "ROLE2", "DESCRIPTION2");
		this
				.doTest(
						"GET",
						"/path?roles{1}.roleName=ROLE1&roles{1}.description=DESCRIPTION1&roles{2}.roleName=ROLE2&roles{2}.description=DESCRIPTION2",
						null, false);
	}

	/**
	 * Ensure can load multiple maps.
	 */
	public void testMultipleMaps() throws Exception {
		this.expectRole("role", "ROLE", "ROLE_DESCRIPTION");
		this.expectProduct("product", "PRODUCT", "PRODUCT_DESCRIPTION");
		this
				.doTest(
						"GET",
						"/path?roles{role}.roleName=ROLE&roles{role}.description=ROLE_DESCRIPTION&products{product}.productName=PRODUCT&products{product}.description=PRODUCT_DESCRIPTION",
						null, false);
	}

	/*
	 * ========================= Helper methods ========================
	 */

	/**
	 * Does the test, expecting mocks to have recorded actions.
	 * 
	 * @param method
	 *            {@link HttpRequest} Method.
	 * @param requestUri
	 *            {@link HttpRequest} Request URI.
	 * @param body
	 *            Body of the {@link HttpRequest}.
	 * @param isCaseSensitive
	 *            Indicates if case sensitive.
	 * @param aliasProperties
	 *            Alias property name pairs.
	 */
	private void doTest(String method, String requestUri, String body,
			boolean isCaseSensitive, String... aliasProperties)
			throws Exception {

		// Record obtaining the map values
		final Map<String, MockRoleBean> roleMap = new HashMap<String, MockRoleBean>();
		final Map<String, MockProductBean> productMap = new HashMap<String, MockProductBean>();
		boolean isMatcherLoader = false;
		for (int i = 0; i < this.roles.size(); i++) {
			this.object.setRoles(null, null);
			if (!isMatcherLoader) {
				this.control(this.object).setMatcher(new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						String key = (String) actual[0];
						MockRoleBean bean = (MockRoleBean) actual[1];
						roleMap.put(key, bean);
						return true;
					}
				});
				isMatcherLoader = true;
			}
		}
		isMatcherLoader = false;
		for (int i = 0; i < this.products.size(); i++) {
			this.object.setProducts(null, null);
			if (!isMatcherLoader) {
				this.control(this.object).setMatcher(new AbstractMatcher() {
					@Override
					public boolean matches(Object[] expected, Object[] actual) {
						String key = (String) actual[0];
						MockProductBean bean = (MockProductBean) actual[1];
						productMap.put(key, bean);
						return true;
					}
				});
				isMatcherLoader = true;
			}
		}

		// Run test to load values
		this.replayMockObjects();
		HttpRequest request = HttpServerTestUtil.createHttpRequest(method,
				requestUri, body);
		HttpParametersLoader<MockInterface> loader = this.createLoader(
				isCaseSensitive, aliasProperties);
		loader.loadParameters(request, this.object);
		this.verifyMockObjects();

		// Verify the roles
		assertEquals("Incorrect number of roles", this.roles.size(), roleMap
				.size());
		for (String key : this.roles.keySet()) {
			MockRoleBean e = this.roles.get(key);
			MockRoleBean a = roleMap.get(key);
			assertNotNull("Expecting role for key '" + key + "'", a);
			assertEquals("Incorrect role name for key '" + key + "'",
					e.roleName, a.roleName);
			assertEquals("Incorrect description for key '" + key + "'",
					e.description, a.description);
		}

		// Verify the products
		assertEquals("Incorrect number of products", this.products.size(),
				productMap.size());
		for (String key : this.products.keySet()) {
			MockProductBean e = this.products.get(key);
			MockProductBean a = productMap.get(key);
			assertNotNull("Expecting product for key '" + key + "'", a);
			assertEquals("Incorrect product name for key '" + key + "'",
					e.productName, a.productName);
			assertEquals("Incorrect description for key '" + key + "'",
					e.description, a.description);
		}
	}

	/**
	 * Creates the initialised {@link HttpParametersLoader} for testing.
	 * 
	 * @param isCaseSensitive
	 *            Indicates if should be case sensitive.
	 * @param aliasProperties
	 *            Alias mappings of alias to property name pairs.
	 * @return Initialised {@link HttpParametersLoader}.
	 */
	private HttpParametersLoader<MockInterface> createLoader(
			boolean isCaseSensitive, String... aliasProperties)
			throws Exception {

		// Create the loader
		HttpParametersLoader<MockInterface> loader = new HttpParametersLoaderImpl<MockInterface>();

		// Create the alias mappings
		Map<String, String> aliasMappings = new HashMap<String, String>();
		for (int i = 0; i < aliasProperties.length; i += 2) {
			String aliasName = aliasProperties[i];
			String propertyName = aliasProperties[i + 1];
			aliasMappings.put(aliasName, propertyName);
		}

		// Initialise the loader
		loader.init(MockInterface.class, aliasMappings, !isCaseSensitive, null);

		// Return the loader
		return loader;
	}

	/**
	 * Creates an expected {@link MockRoleBean}.
	 * 
	 * @param key
	 *            Key to be loaded into the {@link Map}.
	 * @param roleName
	 *            Role name.
	 * @param description
	 *            Description.
	 * @return Expected {@link MockRoleBean}.
	 */
	private MockRoleBean expectRole(String key, String roleName,
			String description) {

		// Create the expected role
		MockRoleBean role = new MockRoleBean();
		role.setRoleName(roleName);
		role.setDescription(description);

		// Register the expected role
		this.roles.put(key, role);

		// Return the expected role
		return role;
	}

	/**
	 * Creates an expected {@link MockProductBean}.
	 * 
	 * @param key
	 *            Key to be loaded into the {@link Map}.
	 * @param productName
	 *            Product name.
	 * @param description
	 *            Description.
	 * @return Expected {@link MockProductBean}.
	 */
	private MockProductBean expectProduct(String key, String productName,
			String description) {

		// Create the expected product
		MockProductBean product = new MockProductBean();
		product.setProductName(productName);
		product.setDescription(description);

		// Register the expected product
		this.products.put(key, product);

		// Return the expected product
		return product;
	}

	/**
	 * Interface of Mock object. Allows to test type rather than class.
	 */
	public static interface MockInterface {

		void setFirstName(String firstName);

		void setLastName(String lastName);

		void setDescription(String description);

		void setMultipleValues(String key, String value);

		void setRoles(String key, MockRoleBean bean);

		void setProducts(String key, MockProductBean bean);

		void setIgnore(int value);
	}

	/**
	 * Role bean within the {@link Map}.
	 */
	public static class MockRoleBean {

		public String roleName;

		public String description;

		public void setRoleName(String roleName) {
			this.roleName = roleName;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	/**
	 * Product bean within the {@link Map}.
	 */
	public static class MockProductBean {

		public String productName;

		public String description;

		public void setProductName(String productName) {
			this.productName = productName;
		}

		public void setDescription(String description) {
			this.description = description;
		}
	}

	/**
	 * Mock object for testing loading.
	 */
	public static class MockObject implements MockInterface {

		/**
		 * First name.
		 */
		public String firstName = null;

		/**
		 * Last name.
		 */
		public String lastName = null;

		/**
		 * Description.
		 */
		public String description = null;

		/**
		 * Roles.
		 */
		public Map<String, MockRoleBean> roles = null;

		/**
		 * Products.
		 */
		public Map<String, MockProductBean> products = null;

		/**
		 * Ensures only the interface (type) methods are loaded.
		 * 
		 * @param value
		 *            String value as required.
		 */
		public void setIgnore(String value) {
			fail("Should not be invoked as not on interface");
		}

		/*
		 * ================ MockInterface ==========================
		 */

		@Override
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		@Override
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		@Override
		public void setDescription(String description) {
			this.description = description;
		}

		@Override
		public void setRoles(String key, MockRoleBean bean) {
			fail("Should not be used in testing");
		}

		@Override
		public void setProducts(String key, MockProductBean bean) {
			fail("Should not be used in testing");
		}

		@Override
		public void setMultipleValues(String key, String value) {
			fail("Should not be used in testing");
		}

		@Override
		public void setIgnore(int value) {
			fail("Should not be invoked as ignored (not String parameter)");
		}
	}

}