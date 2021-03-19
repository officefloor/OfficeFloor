/*-
 * #%L
 * Web Plug-in
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.web.value.load;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link PropertyKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyKeyTest extends OfficeFrameTestCase {

	/**
	 * Flag indicating if case insensitive.
	 */
	private boolean isCaseInSensitive;

	/**
	 * Case sensitive.
	 */
	public void testCaseSensitive() {
		this.isCaseInSensitive = false;
		this.doTest();
	}

	/**
	 * Case insensitive.
	 */
	public void testCaseInsensitive() {
		this.isCaseInSensitive = true;
		this.doTest();
	}

	/**
	 * Undertakes the testing.
	 */
	private void doTest() {

		// Simple property combinations
		this.doTest("property", "property", true);
		this.doTest("Property", "Property", true);
		this.doTest("PROPERTY", "PROPERTY", true);
		this.doTest("Property", "property", this.isCaseInSensitive, true);
		this.doTest("property", "PROPERTY", this.isCaseInSensitive, true);
		this.doTest("property", "mismatch", false);

		// Different lengths
		this.doTest("prop", "property", false);
		this.doTest("property", "prop", false);

		// Stringed together property combinations
		this.doTest("property.one", "property.one", true);
		this.doTest("Property.One", "Property.One", true);
		this.doTest("PROPERTY.ONE", "PROPERTY.ONE", true);
		this.doTest("Property.One", "property.one", this.isCaseInSensitive,
				true);
		this.doTest("property.one", "PROPERTY.ONE", this.isCaseInSensitive,
				true);
		this.doTest("property.one", "mismatch.one", false);

		// Map property combinations
		this.doTest("map{a}", "map{a}", true);
		this.doTest("Map{a}", "Map{a}", true);
		this.doTest("MAP{a}", "MAP{a}", true);
		this.doTest("Map{a}", "map{a}", this.isCaseInSensitive, true);
		this.doTest("map{a}", "MAP{a}", this.isCaseInSensitive, true);
		this.doTest("map{a}", "map{A}", false, true); // key case sensitive
		this.doTest("map{a}", "map{b}", false);

		// Stringed map property combinations
		this.doTest("property.map{a}.text", "property.map{a}.text", true);
		this.doTest("PROPERTY.Map{a}.text", "property.MAP{a}.Text",
				this.isCaseInSensitive, true);
		this.doTest("property.map{a}.text", "property.map{A}.text", false, true);
		this.doTest("property.map{a}.map{b}", "property.map{a}.map{b}", true);
		this.doTest("PROPERTY.Map{a}.map{b}", "property.MAP{a}.Map{b}",
				this.isCaseInSensitive, true);
	}

	/**
	 * Undertake the test.
	 * 
	 * @param propertyOne
	 *            Property one.
	 * @param propertyTwo
	 *            Property two.
	 * @param isMatch
	 *            Indicates if match both on equals and hash.
	 */
	private void doTest(String propertyOne, String propertyTwo, boolean isMatch) {
		this.doTest(propertyOne, propertyTwo, isMatch, isMatch);
	}

	/**
	 * Undertake the test.
	 * 
	 * @param propertyOne
	 *            Property one.
	 * @param propertyTwo
	 *            Property two.
	 * @param isEqual
	 *            Indicates if expected to be equal.
	 * @param isSameHash
	 *            Indicates if same hash expected.
	 */
	private void doTest(String propertyOne, String propertyTwo,
			boolean isEqual, boolean isSameHash) {

		// Create the properties
		PropertyKeyFactory factory = new PropertyKeyFactory(
				this.isCaseInSensitive);
		PropertyKey a = factory.createPropertyKey(propertyOne);
		PropertyKey b = factory.createPropertyKey(propertyTwo);

		// Obtain the hash values
		int hashA = a.hashCode();
		int hashB = b.hashCode();

		// Handle matching
		assertEquals(propertyOne + "=" + propertyTwo, isEqual, a.equals(b));
		assertEquals(hashA + "(" + propertyOne + ")=" + hashB + "("
				+ propertyTwo + ")", isSameHash, hashA == hashB);

		// Determine if can find in collection
		Set<PropertyKey> keys = new HashSet<PropertyKey>();
		keys.add(a);
		assertEquals("Find " + propertyOne + " by " + propertyTwo, isEqual,
				keys.contains(b));
	}
}
