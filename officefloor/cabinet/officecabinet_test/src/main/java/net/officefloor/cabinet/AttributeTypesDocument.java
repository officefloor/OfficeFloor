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

import org.junit.jupiter.api.Assertions;

import lombok.Data;

/**
 * Entity to ensure all attribute types.
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
		this.bytePrimitive = (byte) (offset + 1);
		this.byteObject = (byte) (offset + 2);
		this.shortPrimitive = (short) (offset + 3);
		this.shortObject = (short) (offset + 4);
		this.charPrimitive = (char) (offset + 5);
		this.charObject = (char) (offset + 6);
		this.intPrimitive = offset + 7;
		this.intObject = offset + 8;
		this.longPrimitive = offset + 9;
		this.longObject = Long.valueOf(offset + 10);
		this.floatPrimitive = offset + 11;
		this.floatObject = (float) (offset + 12);
		this.doublePrimitive = offset + 13;
		this.doubleObject = (double) (offset + 14);
	}

	/**
	 * Assets the values match the input {@link AttributeTypesDocument}.
	 * 
	 * @param entity {@link AttributeTypesDocument}.
	 */
	public void assertEquals(AttributeTypesDocument entity) {
		Assertions.assertEquals(this.booleanPrimitive, entity.booleanPrimitive, "boolean");
		Assertions.assertEquals(this.booleanObject, entity.booleanObject, "Boolean");
		Assertions.assertNull(this.booleanNull, "Boolean null");
		Assertions.assertEquals(this.bytePrimitive, entity.bytePrimitive, "byte");
		Assertions.assertEquals(this.byteObject, entity.byteObject, "Byte");
		Assertions.assertNull(this.byteNull, "Byte null");
		Assertions.assertEquals(this.shortPrimitive, entity.shortPrimitive, "short");
		Assertions.assertEquals(this.shortObject, entity.shortObject, "Short");
		Assertions.assertNull(this.shortNull, "Short null");
		Assertions.assertEquals(this.charPrimitive, entity.charPrimitive, "char");
		Assertions.assertEquals(this.charObject, entity.charObject, "Character");
		Assertions.assertNull(this.charNull, "Character null");
		Assertions.assertEquals(this.intPrimitive, entity.intPrimitive, "int");
		Assertions.assertEquals(this.intObject, entity.intObject, "Integer");
		Assertions.assertNull(this.intNull, "Integer null");
		Assertions.assertEquals(this.longPrimitive, entity.longPrimitive, "long");
		Assertions.assertEquals(this.longObject, entity.longObject, "Long");
		Assertions.assertNull(this.longNull, "Long null");
		Assertions.assertEquals(this.floatPrimitive, entity.floatPrimitive, "float");
		Assertions.assertEquals(this.floatObject, entity.floatObject, "Float");
		Assertions.assertNull(this.floatNull, "Float null");
		Assertions.assertEquals(this.doublePrimitive, entity.doublePrimitive, "double");
		Assertions.assertEquals(this.doubleObject, entity.doubleObject, "Double");
		Assertions.assertNull(this.doubleNull, "Double null");
	}

}
