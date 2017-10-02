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
package net.officefloor.web.value.load;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.value.load.ObjectInstantiator;
import net.officefloor.web.value.load.ValueLoader;
import net.officefloor.web.value.load.ValueLoaderFactory;
import net.officefloor.web.value.load.ValueLoaderSource;

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
	private ObjectInstantiator instantiator = this
			.createMock(ObjectInstantiator.class);

	/**
	 * Alias mappings.
	 */
	private Map<String, String> aliasMappings = new HashMap<String, String>();

	/**
	 * Flag indicating if case sensitive.
	 */
	private boolean isCaseSensitive = true;

	/**
	 * Ensure can load a single property.
	 */
	public void testSingleProperty() {
		this.object.setPropertyOne("ONE");
		this.doTest("PropertyOne", "ONE");
	}

	/**
	 * Ensure can load multiple properties.
	 */
	public void testMultipleProperties() {
		this.object.setPropertyOne("A");
		this.object.setPropertyTwo("B");
		this.doTest("PropertyOne", "A", "PropertyTwo", "B");
	}

	/**
	 * Do not load primitive values.
	 */
	public void testIgnorePrimatives() {
		this.doTest("Ignore", "not load");
	}

	/**
	 * Ensure can load properties via aliases.
	 */
	public void testAliasProperties() {
		this.mapAlias("Alias", "PropertyOne");
		this.object.setPropertyOne("VALUE");
		this.doTest("Alias", "VALUE");
	}

	/**
	 * Ensure can load case insensitive properties.
	 */
	public void testCaseInsenstiveProperties() {
		this.isCaseSensitive = false;
		this.object.setPropertyOne("Lower");
		this.object.setPropertyTwo("Upper");
		this.doTest("propertyone", "Lower", "PROPERTYTWO", "Upper");
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
		this.doTest("AliasOne", "One", "AliasTwo", "Two");
	}

	/**
	 * Ensure can string property names together to follow graph of objects.
	 */
	public void testStringedTogetherProperty() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		objectOne.setPropertyA("VALUE");
		this.doTest("ObjectOne.PropertyA", "VALUE");
	}

	/**
	 * Ensure can load multiple stringed together properties.
	 */
	public void testMultipleStringedTogetherProperties() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setObjectOne(objectOne);
		objectOne.setPropertyA("a");
		objectOne.setPropertyB("b");
		this.doTest("ObjectOne.PropertyA", "a", "ObjectOne.PropertyB", "b");
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
		this.doTest("AliasOne.AliasA", "VALUE");
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
		this.doTest("objectone.propertya", "Lower", "OBJECTONE.PROPERTYB",
				"Upper");
	}

	/**
	 * Ensure can load keyed property.
	 */
	public void testKeyedProperty() {
		this.object.setKeyOne("KEY", "VALUE");
		this.doTest("KeyOne{KEY}", "VALUE");
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
		this.doTest("KeyOne{0}", "FIRST", "KeyOne{1}", "SECOND", "KeyOne{2}",
				"THIRD");
	}

	/**
	 * Ensure can load alias property for a keyed value.
	 */
	public void testKeysForAliasProperty() {
		this.mapAlias("AliasOne", "KeyOne");
		this.object.setKeyOne("KEY", "VALUE");
		this.doTest("AliasOne{KEY}", "VALUE");
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
		this.doTest("keyone{case}", "Lower", "KEYONE{CASE}", "Upper",
				"KeyOne{Case}", "Title");
	}

	/**
	 * Ensure can load mapped object property.
	 */
	public void testMappedObjectProperty() {
		MockObjectOne objectOne = this.record_instantiate(MockObjectOne.class);
		this.object.setMapOne("KEY", objectOne);
		objectOne.setPropertyA("VALUE");
		this.doTest("MapOne{KEY}.PropertyA", "VALUE");
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
		this.doTest("MapOne{1}.PropertyA", "ONE", "MapOne{2}.PropertyA", "TWO");
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
		this.doTest("AliasOne{KEY}.AliasA", "VALUE");
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
		this.doTest("mapone{case}.propertya", "Lower",
				"MAPONE{CASE}.PROPERTYA", "Upper");
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
		this.doTest("Recursive.Recursive.Recursive.Value", "VALUE");
	}

	/**
	 * Maps an alias for a name.
	 * 
	 * @param alias
	 *            Alias.
	 * @param name
	 *            Name.
	 */
	private void mapAlias(String alias, String name) {
		this.aliasMappings.put(alias, name);
	}

	/**
	 * Records instantiating an object.
	 * 
	 * @param clazz
	 *            {@link Class} of object to instantiate.
	 * @return Instantiated object.
	 */
	private <T> T record_instantiate(Class<T> clazz) {
		try {
			// Create the mock object to load
			T mock = this.createMock(clazz);

			// Record instantiating the object
			this.recordReturn(this.instantiator,
					this.instantiator.instantiate(clazz), mock);

			// Return the instantiated object
			return mock;
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Does the test.
	 * 
	 * @param nameValuePairs
	 *            Name value pairs for properties to load.
	 */
	private void doTest(String... nameValuePairs) {
		this.replayMockObjects();
		try {
			// Create the source
			ValueLoaderSource source = new ValueLoaderSource(MockType.class, !this.isCaseSensitive,
					this.aliasMappings, this.instantiator);

			// Create and return the value loader
			ValueLoaderFactory<MockType> factory = source
					.sourceValueLoaderFactory(MockType.class);
			ValueLoader valueLoader = factory.createValueLoader(this.object);

			// Load the values
			for (int i = 0; i < nameValuePairs.length; i += 2) {
				String name = nameValuePairs[i];
				String value = nameValuePairs[i + 1];
				valueLoader.loadValue(name, value);
			}

		} catch (Exception ex) {
			fail(ex);
		}
		this.verifyMockObjects();
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
	}

	/**
	 * Mock object one for loading.
	 */
	public static interface MockObjectOne {

		void setPropertyA(String propertyA);

		void setPropertyB(String propertyB);
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

}