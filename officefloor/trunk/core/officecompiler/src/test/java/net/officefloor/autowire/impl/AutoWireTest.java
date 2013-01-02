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
package net.officefloor.autowire.impl;

import java.lang.annotation.Documented;

import net.officefloor.autowire.AutoWire;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public class AutoWireTest extends OfficeFrameTestCase {

	/**
	 * Default type.
	 */
	public void testDefaultType() {
		verifyAutoWire(new AutoWire("TYPE"), null, "TYPE", "TYPE");
	}

	/**
	 * Qualified type.
	 */
	public void testQualifiedType() {
		verifyAutoWire(new AutoWire("QUALIFIED", "TYPE"), "QUALIFIED", "TYPE",
				"QUALIFIED-TYPE");
	}

	/**
	 * Default safe type.
	 */
	public void testDefaultSafeType() {
		verifyAutoWire(new AutoWire(Object.class), null, "java.lang.Object",
				"java.lang.Object");
	}

	/**
	 * Qualified safe type.
	 */
	public void testQualifiedSafeType() {
		verifyAutoWire(new AutoWire(Documented.class, Object.class),
				"java.lang.annotation.Documented", "java.lang.Object",
				"java.lang.annotation.Documented-java.lang.Object");
	}

	/**
	 * Validate the object methods
	 */
	public void testObjectMethods() {

		// Validate matching
		AutoWire autoWire = new AutoWire(Documented.class, Object.class);
		AutoWire match = new AutoWire(Documented.class, Object.class);
		assertEquals("Ensure qualified match", autoWire, match);
		assertEquals("Ensure qualified hash code match", autoWire.hashCode(),
				match.hashCode());

		// Validate not matching
		assertFalse("No qualifier", autoWire.equals(new AutoWire(Object.class)));
		assertFalse("Incorrect type",
				autoWire.equals(new AutoWire(Documented.class, String.class)));

		// Validate match without qualifier
		autoWire = new AutoWire(Object.class);
		match = new AutoWire(Object.class);
		assertEquals("Ensure match", autoWire, match);
		assertEquals("Ensure hash code match", autoWire.hashCode(),
				match.hashCode());
	}

	/**
	 * Ensure can appropriately create {@link AutoWire} from its qualified type.
	 */
	public void testValueOf() {

		// Unqualified
		validateValueOf(new AutoWire(Object.class));

		// Qualified
		validateValueOf(new AutoWire(Documented.class, Object.class));

		// As last is typically a class it should not have a '-'
		validateValueOf(new AutoWire("qualifier-with-hyphens", "type"));

		// Edge cases
		validateValueOf(new AutoWire("notype", ""));
	}

	/**
	 * Validates the {@link AutoWire#valueOf(String)}.
	 * 
	 * @param autoWire
	 *            {@link AutoWire} to use to get qualified type to test.
	 */
	private static void validateValueOf(AutoWire autoWire) {
		verifyAutoWire(AutoWire.valueOf(autoWire.getQualifiedType()),
				autoWire.getQualifier(), autoWire.getType(),
				autoWire.getQualifiedType());
	}

	/**
	 * Verifies the {@link AutoWire}.
	 * 
	 * @param wire
	 *            {@link AutoWire} to verify.
	 * @param expectedQualifier
	 *            Expected qualifier.
	 * @param expectedType
	 *            Expected type.
	 * @param expectedQualifiedType
	 *            Expected qualified type.
	 */
	private static void verifyAutoWire(AutoWire wire, String expectedQualifier,
			String expectedType, String expectedQualifiedType) {
		assertEquals("Incorrect qualifier", expectedQualifier,
				wire.getQualifier());
		assertEquals("Incorrect type", expectedType, wire.getType());
		assertEquals("Incorrect qualified type", expectedQualifiedType,
				wire.getQualifiedType());
	}

}