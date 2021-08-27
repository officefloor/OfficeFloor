/*-
 * #%L
 * OfficeFloor Filing Cabinet Test
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.cabinet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import lombok.Data;

/**
 * {@link Document} to ensure all attribute types.
 * 
 * @author Daniel Sagenschneider
 */
@Data
@Document
public class AttributeTypesDocument {

	@Key
	private String key;

	private boolean booleanPrimitive;

	private Boolean booleanObject;

	private Boolean booleanNull = null;

	private byte bytePrimitive;

	private Byte byteObject;

	private Byte byteNull = null;

	private short shortPrimitive;

	private Short shortObject;

	private Short shortNull = null;

	private char charPrimitive;

	private Character charObject;

	private Character charNull = null;

	private int intPrimitive;

	private Integer intObject;

	private Integer intNull = null;

	private long longPrimitive;

	private Long longObject;

	private Long longNull = null;

	private float floatPrimitive;

	private Float floatObject;

	private Float floatNull = null;

	private double doublePrimitive;

	private Double doubleObject;

	private Double doubleNull = null;

	public AttributeTypesDocument() {
	}

	public AttributeTypesDocument(int offset) {
		this.booleanPrimitive = (offset % 2) == 0;
		this.booleanObject = !this.booleanPrimitive;
		this.bytePrimitive = (byte) (++offset);
		this.byteObject = (byte) (++offset);
		this.shortPrimitive = (short) (++offset);
		this.shortObject = (short) (++offset);
		this.charPrimitive = (char) (++offset);
		this.charObject = (char) (++offset);
		this.intPrimitive = ++offset;
		this.intObject = ++offset;
		this.longPrimitive = ++offset;
		this.longObject = Long.valueOf(++offset);
		this.floatPrimitive = ++offset;
		this.floatObject = (float) (++offset);
		this.doublePrimitive = ++offset;
		this.doubleObject = (double) (++offset);
	}

	/**
	 * Assets the values match the input {@link AttributeTypesDocument}.
	 * 
	 * @param document {@link AttributeTypesDocument}.
	 */
	public void assertDocumentEquals(AttributeTypesDocument document) {
		assertEquals(this.booleanPrimitive, document.booleanPrimitive, "boolean");
		assertEquals(this.booleanObject, document.booleanObject, "Boolean");
		assertNull(this.booleanNull, "Boolean null");
		assertEquals(this.bytePrimitive, document.bytePrimitive, "byte");
		assertEquals(this.byteObject, document.byteObject, "Byte");
		assertNull(this.byteNull, "Byte null");
		assertEquals(this.shortPrimitive, document.shortPrimitive, "short");
		assertEquals(this.shortObject, document.shortObject, "Short");
		assertNull(this.shortNull, "Short null");
		assertEquals(this.charPrimitive, document.charPrimitive, "char");
		assertEquals(this.charObject, document.charObject, "Character");
		assertNull(this.charNull, "Character null");
		assertEquals(this.intPrimitive, document.intPrimitive, "int");
		assertEquals(this.intObject, document.intObject, "Integer");
		assertNull(this.intNull, "Integer null");
		assertEquals(this.longPrimitive, document.longPrimitive, "long");
		assertEquals(this.longObject, document.longObject, "Long");
		assertNull(this.longNull, "Long null");
		assertEquals(this.floatPrimitive, document.floatPrimitive, "float");
		assertEquals(this.floatObject, document.floatObject, "Float");
		assertNull(this.floatNull, "Float null");
		assertEquals(this.doublePrimitive, document.doublePrimitive, "double");
		assertEquals(this.doubleObject, document.doubleObject, "Double");
		assertNull(this.doubleNull, "Double null");
	}

}
