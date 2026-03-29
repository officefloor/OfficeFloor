/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.impl.type;

import java.lang.reflect.Method;

import net.officefloor.compile.type.AnnotatedType;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.Next;

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
	@Next("TEST")
	public void testMatchActualAnnotation() throws Exception {
		Method method = this.getClass().getMethod("testMatchActualAnnotation");
		Next nextFunction = method.getAnnotation(Next.class);
		assertNotNull("Invalid test as not found annotation", nextFunction);
		AnnotatedType type = () -> method.getAnnotations();
		assertEquals("Incorrect annotation", nextFunction, type.getAnnotation(Next.class));
	}

}
