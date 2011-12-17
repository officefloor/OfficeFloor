/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
package net.officefloor.plugin.autowire;

import java.lang.annotation.Documented;

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
		this.verifyAutoWire(new AutoWire("TYPE"), null, "TYPE", "TYPE");
	}

	/**
	 * Qualified type.
	 */
	public void testQualifiedType() {
		this.verifyAutoWire(new AutoWire("QUALIFIED", "TYPE"), "QUALIFIED",
				"TYPE", "QUALIFIED-TYPE");
	}

	/**
	 * Default safe type.
	 */
	public void testDefaultSafeType() {
		this.verifyAutoWire(new AutoWire(Object.class), null,
				"java.lang.Object", "java.lang.Object");
	}

	/**
	 * Qualified safe type.
	 */
	public void testQualifiedSafeType() {
		this.verifyAutoWire(new AutoWire(Documented.class, Object.class),
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
	private void verifyAutoWire(AutoWire wire, String expectedQualifier,
			String expectedType, String expectedQualifiedType) {
		assertEquals("Incorrect qualifier", expectedQualifier,
				wire.getQualifier());
		assertEquals("Incorrect type", expectedType, wire.getType());
		assertEquals("Incorrect qualified type", expectedQualifiedType,
				wire.getQualifiedType());
	}

}