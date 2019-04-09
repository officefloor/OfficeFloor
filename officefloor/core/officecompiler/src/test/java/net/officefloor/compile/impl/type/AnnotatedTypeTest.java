/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.compile.impl.type;

import java.lang.reflect.Method;

import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextFunction;

/**
 * Tests the {@link AnnotatedType}.
 * 
 * @author Daniel Sagenschneider
 */
public class AnnotatedTypeTest extends OfficeFrameTestCase {

	/**
	 * Ensure handle no annotations.
	 */
	public void testNoAnnotations() {
		AnnotatedType type = () -> new Object[0];
		assertNull("Should not find annotation", type.getAnnotation(Object.class));
	}

	/**
	 * Ensure handle <code>null</code> annotations.
	 */
	public void testNullAnnotations() {
		AnnotatedType type = () -> null;
		assertNull("Should not find annotation", type.getAnnotation(Object.class));
	}

	/**
	 * Ensure able to retrieve the annotation.
	 */
	public void testMatchAnnotation() {
		AnnotatedType type = () -> new Object[] { "TEST" };
		assertEquals("Incorrect annotation", "TEST", type.getAnnotation(String.class));
	}

	/**
	 * Ensure able to match exact first.
	 */
	public void testMatchExactFirst() {
		Object object = new Object();
		AnnotatedType type = () -> new Object[] { "TEST", object };
		assertEquals("Incorrect annotation", object, type.getAnnotation(Object.class));
	}

	public void testMatchFirstChild() {
		AnnotatedType type = () -> new Object[] { "FIRST", "SECOND" };
		assertEquals("Incorrect annotation", "FIRST", type.getAnnotation(Object.class));
	}

	/**
	 * Ensure able to match actual annotation.
	 */
	@NextFunction("TEST")
	public void testMatchActualAnnotation() throws Exception {
		Method method = this.getClass().getMethod("testMatchActualAnnotation");
		NextFunction nextFunction = method.getAnnotation(NextFunction.class);
		assertNotNull("Invalid test as not found annotation", nextFunction);
		AnnotatedType type = () -> method.getAnnotations();
		assertEquals("Incorrect annotation", nextFunction, type.getAnnotation(NextFunction.class));
	}

}