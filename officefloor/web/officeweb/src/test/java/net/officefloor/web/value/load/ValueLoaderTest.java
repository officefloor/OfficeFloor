/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.value.load;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.HttpContentParameter;
import net.officefloor.web.HttpCookieParameter;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.build.HttpValueLocation;

/**
 * Tests loading values via a {@link ValueLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueLoaderTest extends OfficeFrameTestCase {

	/**
	 * Root object to load values.
	 */
	private MockType object = this.createMock(MockType.class);

	/**
	 * Mock {@link ObjectInstantiator}.
	 */
	private ObjectInstantiator instantiator = this.createMock(ObjectInstantiator.class);

	/**
	 * Alias mappings.
	 */
	private Map<String, String> aliasMappings = new HashMap<String, String>();

	/**
	 * Flag indicating if case sensitive.
	 */
	private boolean isCaseSensitive = true;

	/**
	 * Ensure can list all {@link ValueName} instances.
	 */
	public void testListValueNames() throws Exception {

		// Ensure list aliases
		this.mapAlias("Alias", "PropertyOne");

		// Create value loader factory
		ValueLoaderSource source = new ValueLoaderSource(MockType.class, !this.isCaseSensitive, this.aliasMappings,
				this.instantiator);
		ValueLoaderFactory<MockType> factory = source.sourceValueLoaderFactory(MockType.class);

		// Should have all properties
		Map<String, HttpValueLocation> expected = new HashMap<>();
		expected.put("PropertyOne", null);
		expected.put("Alias", null);
		expected.put("PropertyTwo", null);
		expected.put("ObjectOne.PropertyA", null);
		expected.put("ObjectOne.PropertyB", null);
		expected.put("ObjectOne.PathOnly", HttpValueLocation.PATH);
		expected.put("ObjectTwo.Property1", null);
		expected.put("ObjectTwo.ObjectOne.PropertyA", null);
		expected.put("ObjectTwo.ObjectOne.PropertyB", null);
		expected.put("ObjectTwo.ObjectOne.PathOnly", HttpValueLocation.PATH);
		expected.put("KeyOne{}", null);
		expected.put("KeyTwo{}", null);
		expected.put("MapOne{}.PropertyA", null);
		expected.put("MapOne{}.PropertyB", null);
		expected.put("MapOne{}.PathOnly", HttpValueLocation.PATH);
		expected.put("MapTwo{}.Property1", null);
		expected.put("MapTwo{}.ObjectOne.PropertyA", null);
		expected.put("MapTwo{}.ObjectOne.PropertyB", null);
		expected.put("MapTwo{}.ObjectOne.PathOnly", HttpValueLocation.PATH);
		expected.put("Recursive.Recursive.Recursive...", null);
		expected.put("Recursive.Recursive.Value", null);
		expected.put("Recursive.Value", null);
		expected.put("PathOnly", HttpValueLocation.PATH);
		expected.put("KeyPathOnly{}", HttpValueLocation.PATH);
		expected.put("QueryOnly", HttpValueLocation.QUERY);
		expected.put("KeyQueryOnly{}", HttpValueLocation.QUERY);
		expected.put("HeaderOnly", HttpValueLocation.HEADER);
		expected.put("KeyHeaderOnly{}", HttpValueLocation.HEADER);
		expected.put("CookieOnly", HttpValueLocation.COOKIE);
		expected.put("KeyCookieOnly{}", HttpValueLocation.COOKIE);
		expected.put("ContentOnly", HttpValueLocation.ENTITY);
		expected.put("KeyContentOnly{}", HttpValueLocation.ENTITY);
		expected.put("Field.Property", HttpValueLocation.PATH);

		// Ensure all properties are configured
		ValueName[] actual = factory.getValueNames();
		Set<String> uniqueValueNames = new HashSet<>();
		for (ValueName valueName : actual) {
			String name = valueName.getName();
			assertTrue("Unexpected name: " + name, expected.containsKey(name));
			assertEquals("Incorrect location for " + name, expected.get(name), valueName.getLocation());
			uniqueValueNames.add(name);
		}
		assertEquals("Incorrect number of names", expected.size(), uniqueValueNames.size());
	}

	/**
	 * Ensure can load a single property.
	 */
	public void testSingleProperty() {
		this.object.setPropertyOne("ONE");
		this.doTest(V("PropertyOne", "ONE"));
	}

	/**
	 * Ensure can load multiple properties.
	 */
	public void testMultipleProperties() {
		this.object.setPropertyOne("A");
		this.object.setPropertyTwo("B");
		this.doTest(V("PropertyOne", "A"), V("PropertyTwo", "B"));
	}

	/**
	 * Do not load primitive values.
	 */
	public void testIgnorePrimatives() {
		this.doTest(V("Ignore", "not load"));
	}

	/**
	 * Ensure can load properties via aliases.
	 */
	public void testAliasProperties() {
		this.mapAlias("Alias", "PropertyOne");
		this.object.setPropertyOne("VALUE");
		this.doTest(V("Alias", "VALUE"));
	}

	/**
	 * Ensure can load case insensitive properties.
	 */
	public void testCaseInsenstiveProperties() {
		this.isCaseSensitive = false;
		this.object.setPropertyOne("Lower");
		this.object.setPropertyTwo("Upper");
		this.doTest(V("propertyone", "Lower"), V("PROPERTYTWO", "Upper"));
	}

	/**
	 * Ensure can have case insensitive alias mappings.
	 */
	public void testCaseInsensitiveAlias() {
		this.isCaseSensitive = false;
		this.mapAlias("aliasone", "propertyone");
		this.mapAlias("ALIASTWO", "PROPERTYTWO");
		this.object.setPropertyOne("One");
		this.object.setPropertyTwo("Two");
		this.doTest(V("AliasOne", "One"), V("AliasTwo", "Two"));
	}

	/**
	 * Ensure can string property names together to follow graph of objects.
	 */
	public void testStringedTogetherProperty() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		objectOne.setPropertyA("VALUE");
		this.doTest(V("ObjectOne.PropertyA", "VALUE"));
	}

	/**
	 * Ensure can load multiple stringed together properties.
	 */
	public void testMultipleStringedTogetherProperties() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		objectOne.setPropertyA("a");
		objectOne.setPropertyB("b");
		this.doTest(V("ObjectOne.PropertyA", "a"), V("ObjectOne.PropertyB", "b"));
	}

	/**
	 * Ensure can load a stringed together property via aliases.
	 */
	public void testAliasStringedTogetherProperty() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		this.mapAlias("AliasOne", "ObjectOne");
		this.mapAlias("AliasA", "PropertyA");
		objectOne.setPropertyA("VALUE");
		this.doTest(V("AliasOne.AliasA", "VALUE"));
	}

	/**
	 * Ensure can load case insensitive multiple stringed together properties.
	 */
	public void testCaseInsensitiveMultipleStringedTogetherProperties() {
		this.isCaseSensitive = false;
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		objectOne.setPropertyA("Lower");
		objectOne.setPropertyB("Upper");
		this.doTest(V("objectone.propertya", "Lower"), V("OBJECTONE.PROPERTYB", "Upper"));
	}

	/**
	 * Ensure can load keyed property.
	 */
	public void testKeyedProperty() {
		this.object.setKeyOne("KEY", "VALUE");
		this.doTest(V("KeyOne{KEY}", "VALUE"));
	}

	/**
	 * <p>
	 * Ensure can load multiple values for different keys for a property.
	 * <p>
	 * Also demonstrates how to load a list of values.
	 */
	public void testMultipleKeysForProperty() {
		this.object.setKeyOne("0", "FIRST");
		this.object.setKeyOne("1", "SECOND");
		this.object.setKeyOne("2", "THIRD");
		this.doTest(V("KeyOne{0}", "FIRST"), V("KeyOne{1}", "SECOND"), V("KeyOne{2}", "THIRD"));
	}

	/**
	 * Ensure can load alias property for a keyed value.
	 */
	public void testKeysForAliasProperty() {
		this.mapAlias("AliasOne", "KeyOne");
		this.object.setKeyOne("KEY", "VALUE");
		this.doTest(V("AliasOne{KEY}", "VALUE"));
	}

	/**
	 * <p>
	 * Ensure can load case insensitive multiple values for different keys for a
	 * property.
	 * <p>
	 * Keys however always maintain their case.
	 */
	public void testCaseInsensitiveMultipleKeysForProperty() {
		this.isCaseSensitive = false;
		this.object.setKeyOne("case", "Lower");
		this.object.setKeyOne("CASE", "Upper");
		this.object.setKeyOne("Case", "Title");
		this.doTest(V("keyone{case}", "Lower"), V("KEYONE{CASE}", "Upper"), V("KeyOne{Case}", "Title"));
	}

	/**
	 * Ensure can load mapped object property.
	 */
	public void testMappedObjectProperty() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("KEY", objectOne);
		objectOne.setPropertyA("VALUE");
		this.doTest(V("MapOne{KEY}.PropertyA", "VALUE"));
	}

	/**
	 * Ensure can load multiple mapped object property.
	 */
	public void testMultipleMappedObjectProperty() {
		MockObjectOne one = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("1", one);
		one.setPropertyA("ONE");
		MockObjectOne two = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("2", two);
		two.setPropertyA("TWO");
		this.doTest(V("MapOne{1}.PropertyA", "ONE"), V("MapOne{2}.PropertyA", "TWO"));
	}

	/**
	 * Ensure can load mapped object property via aliases.
	 */
	public void testAliasMappedObjectProperty() {
		this.mapAlias("AliasOne", "MapOne");
		this.mapAlias("AliasA", "PropertyA");
		MockObjectOne one = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("KEY", one);
		one.setPropertyA("VALUE");
		this.doTest(V("AliasOne{KEY}.AliasA", "VALUE"));
	}

	/**
	 * Ensure can load case insensitive multiple mapped object property.
	 */
	public void testCaseInsensitiveMultipleMappedObjectProperty() {
		this.isCaseSensitive = false;
		MockObjectOne one = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("case", one);
		one.setPropertyA("Lower");
		MockObjectOne two = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("CASE", two);
		two.setPropertyA("Upper");
		this.doTest(V("mapone{case}.propertya", "Lower"), V("MAPONE{CASE}.PROPERTYA", "Upper"));
	}

	/**
	 * Ensure can load recursive value.
	 */
	public void testRecursiveLoad() {
		MockRecursive one = this.record_instantiate(MockRecursive.class);
		this.object.setRecursive(one);
		MockRecursive two = this.record_instantiate(MockRecursive.class);
		one.setRecursive(two);
		MockRecursive three = this.record_instantiate(MockRecursive.class);
		two.setRecursive(three);
		three.setValue("VALUE");
		this.doTest(V("Recursive.Recursive.Recursive.Value", "VALUE"));
	}

	/**
	 * Ensure load only from path location.
	 */
	public void testPathOnly() {
		this.object.setPathOnly("value");
		this.doTest(V("PathOnly", "value", HttpValueLocation.PATH), V("PathOnly", "not path", HttpValueLocation.QUERY));
	}

	/**
	 * Ensure can load keyed path parameter.
	 */
	public void testKeyedPathOnly() {
		this.object.setKeyPathOnly("KEY", "VALUE");
		this.doTest(V("KeyPathOnly{KEY}", "VALUE", HttpValueLocation.PATH),
				V("KeyPathOnly{KEY}", "not path", HttpValueLocation.QUERY));
	}

	/**
	 * Ensure load only from query location.
	 */
	public void testQueryOnly() {
		this.object.setQueryOnly("value");
		this.doTest(V("QueryOnly", "value", HttpValueLocation.QUERY),
				V("QueryOnly", "not query", HttpValueLocation.PATH));
	}

	/**
	 * Ensure can load keyed query parameter.
	 */
	public void testKeyedQueryOnly() {
		this.object.setKeyQueryOnly("KEY", "VALUE");
		this.doTest(V("KeyQueryOnly{KEY}", "VALUE", HttpValueLocation.QUERY),
				V("KeyQueryOnly{KEY}", "not path", HttpValueLocation.PATH));
	}

	/**
	 * Ensure load only from header location.
	 */
	public void testHeaderOnly() {
		this.object.setHeaderOnly("value");
		this.doTest(V("HeaderOnly", "value", HttpValueLocation.HEADER),
				V("HeaderOnly", "not header", HttpValueLocation.PATH));
	}

	/**
	 * Ensure can load keyed header parameter.
	 */
	public void testKeyedHeaderOnly() {
		this.object.setKeyHeaderOnly("KEY", "VALUE");
		this.doTest(V("KeyHeaderOnly{KEY}", "VALUE", HttpValueLocation.HEADER),
				V("KeyHeaderOnly{KEY}", "not header", HttpValueLocation.PATH));
	}

	/**
	 * Ensure load only from cookie location.
	 */
	public void testCookieOnly() {
		this.object.setCookieOnly("value");
		this.doTest(V("CookieOnly", "value", HttpValueLocation.COOKIE),
				V("CookieOnly", "not header", HttpValueLocation.PATH));
	}

	/**
	 * Ensure can load keyed cookie parameter.
	 */
	public void testKeyedCookieOnly() {
		this.object.setKeyCookieOnly("KEY", "VALUE");
		this.doTest(V("KeyCookieOnly{KEY}", "VALUE", HttpValueLocation.COOKIE),
				V("KeyCookieOnly{KEY}", "not header", HttpValueLocation.PATH));
	}

	/**
	 * Ensure load only from content location.
	 */
	public void testContentOnly() {
		this.object.setContentOnly("value");
		this.doTest(V("ContentOnly", "value", HttpValueLocation.ENTITY),
				V("ContentOnly", "not content", HttpValueLocation.PATH));
	}

	/**
	 * Ensure can load keyed content parameter.
	 */
	public void testKeyedContentOnly() {
		this.object.setKeyContentOnly("KEY", "VALUE");
		this.doTest(V("KeyContentOnly{KEY}", "VALUE", HttpValueLocation.ENTITY),
				V("KeyContentOnly{KEY}", "not content", HttpValueLocation.PATH));
	}

	/**
	 * Ensure not create objects for graph if not match location.
	 */
	public void testStringedTogetherPropertyWithLocation() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		// property not loaded, as not in location
		this.doTest(V("ObjectOne.PathOnly", "VALUE", HttpValueLocation.QUERY));
	}

	/**
	 * Ensure can use annotation of the {@link Field}.
	 */
	public void testFieldAnnotation() throws Exception {
		MockField field = new MockField();
		this.recordReturn(this.instantiator, this.instantiator.instantiate(MockField.class), field);
		this.object.setField(field);
		this.doTest(V("Field.Property", "value", HttpValueLocation.PATH),
				V("Field.Property", "not path", HttpValueLocation.QUERY));
		assertEquals("Incorrect field value", "value", field.property);
	}

	/**
	 * Maps an alias for a name.
	 * 
	 * @param alias Alias.
	 * @param name  Name.
	 */
	private void mapAlias(String alias, String name) {
		this.aliasMappings.put(alias, name);
	}

	/**
	 * Records instantiating an object.
	 * 
	 * @param clazz {@link Class} of object to instantiate.
	 * @return Instantiated object.
	 */
	private <T> T record_instantiate(Class<T> clazz) {
		try {
			// Create the mock object to load
			T mock = this.createMock(clazz);

			// Record instantiating the object
			this.recordReturn(this.instantiator, this.instantiator.instantiate(clazz), mock);

			// Return the instantiated object
			return mock;
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Does the test.
	 * 
	 * @param values {@link Value} instances to load.
	 */
	private void doTest(Value... values) {
		this.replayMockObjects();
		try {
			// Create the source
			ValueLoaderSource source = new ValueLoaderSource(MockType.class, !this.isCaseSensitive, this.aliasMappings,
					this.instantiator);

			// Create and return the value loader
			ValueLoaderFactory<MockType> factory = source.sourceValueLoaderFactory(MockType.class);
			ValueLoader valueLoader = factory.createValueLoader(this.object);

			// Load the values
			for (Value value : values) {
				valueLoader.loadValue(value.name, value.value, value.location);
			}

		} catch (Exception ex) {
			fail(ex);
		}
		this.verifyMockObjects();
	}

	/**
	 * Convenience method to construct a {@link Value}.
	 * 
	 * @param name  Name for {@link Value}.
	 * @param value Value for {@link Value}.
	 * @return {@link Value}.
	 */
	public static Value V(String name, String value) {
		return new Value(name, value, HttpValueLocation.ENTITY);
	}

	/**
	 * Convenience method to construct a {@link Value}.
	 * 
	 * @param name     Name for {@link Value}.
	 * @param value    Value for {@link Value}.
	 * @param location {@link HttpValueLocation}.
	 * @return {@link Value}.
	 */
	public static Value V(String name, String value, HttpValueLocation location) {
		return new Value(name, value, location);
	}

	/**
	 * Value to load.
	 */
	private static class Value {

		private final String name;

		private final String value;

		private final HttpValueLocation location;

		public Value(String name, String value, HttpValueLocation location) {
			this.name = name;
			this.value = value;
			this.location = location;
		}
	}

	/**
	 * Root type for loading state.
	 */
	public static interface MockType {

		void setPropertyOne(String propertyOne);

		void setPropertyTwo(String propertyType);

		void setObjectOne(MockObjectOne objectOne);

		void setObjectTwo(MockObjectTwo objectTwo);

		void setKeyOne(String keyTwo, String valueOne);

		void setKeyTwo(String keyTwo, String valueTwo);

		void setMapOne(String key, MockObjectOne objectOne);

		void setMapTwo(String key, MockObjectTwo objectTwo);

		void setRecursive(MockRecursive recursive);

		void setIgnore(int value);

		@HttpPathParameter("")
		void setPathOnly(String pathArgument);

		@HttpPathParameter("")
		void setKeyPathOnly(String key, String value);

		@HttpQueryParameter("")
		void setQueryOnly(String queryArgument);

		@HttpQueryParameter("")
		void setKeyQueryOnly(String key, String value);

		@HttpHeaderParameter("")
		void setHeaderOnly(String headerArgument);

		@HttpHeaderParameter("")
		void setKeyHeaderOnly(String key, String value);

		@HttpCookieParameter("")
		void setCookieOnly(String cookieArgument);

		@HttpCookieParameter("")
		void setKeyCookieOnly(String key, String value);

		@HttpContentParameter("")
		void setContentOnly(String contentArgument);

		@HttpContentParameter("")
		void setKeyContentOnly(String key, String value);

		void setField(MockField field);
	}

	/**
	 * Mock object one for loading.
	 */
	public static interface MockObjectOne {

		void setPropertyA(String propertyA);

		void setPropertyB(String propertyB);

		@HttpPathParameter("")
		void setPathOnly(String value);
	}

	/**
	 * Mock object two for loading.
	 */
	public static interface MockObjectTwo {

		void setProperty1(String property1);

		void setObjectOne(MockObjectOne objectOne);
	}

	/**
	 * Mock recursive object for loading.
	 */
	public static interface MockRecursive {

		void setRecursive(MockRecursive recursive);

		void setValue(String value);
	}

	/**
	 * Mock object with field annotated parameter.
	 */
	public static class MockField {

		@HttpPathParameter("")
		private String property;

		public void setProperty(String value) {
			this.property = value;
		}
	}

}
