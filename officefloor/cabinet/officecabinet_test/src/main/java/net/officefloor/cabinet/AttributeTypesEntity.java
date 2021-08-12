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
public class AttributeTypesEntity {

	@Key
	private String id;

	private boolean booleanPrimitive;

	private byte bytePrimitive;

	private short shortPrimitive;

	private char charPrimitive;

	private int intPrimitive;

	private long longPrimitive;

	private float floatPrimitive;

	private double doublePrimitive;

	public AttributeTypesEntity() {
	}

	public AttributeTypesEntity(int offset) {
		this.booleanPrimitive = (offset % 2) == 0;
		this.bytePrimitive = (byte) (offset + 1);
		this.shortPrimitive = (short) (offset + 2);
		this.charPrimitive = (char) (offset + 3);
		this.intPrimitive = offset + 4;
		this.longPrimitive = offset + 5;
		this.floatPrimitive = offset + 6;
		this.doublePrimitive = offset + 7;
	}

	public AttributeTypesEntity(boolean booleanPrimitive, byte bytePrimitive, short shortPrimitive, char charPrimitive,
			int intPrimitive, long longPrimitive, float floatPrimitive, double doublePrimitive) {
		this.booleanPrimitive = booleanPrimitive;
		this.bytePrimitive = bytePrimitive;
		this.shortPrimitive = shortPrimitive;
		this.charPrimitive = charPrimitive;
		this.intPrimitive = intPrimitive;
		this.longPrimitive = longPrimitive;
		this.floatPrimitive = floatPrimitive;
		this.doublePrimitive = doublePrimitive;
	}

	/**
	 * Assets the values match the input {@link AttributeTypesEntity}.
	 * 
	 * @param entity {@link AttributeTypesEntity}.
	 */
	public void assertEquals(AttributeTypesEntity entity) {
		Assertions.assertEquals(this.booleanPrimitive, entity.booleanPrimitive, "boolean");
		Assertions.assertEquals(this.bytePrimitive, entity.bytePrimitive, "byte");
		Assertions.assertEquals(this.shortPrimitive, entity.shortPrimitive, "short");
		Assertions.assertEquals(this.charPrimitive, entity.charPrimitive, "char");
		Assertions.assertEquals(this.intPrimitive, entity.intPrimitive, "int");
		Assertions.assertEquals(this.longPrimitive, entity.longPrimitive, "long");
		Assertions.assertEquals(this.floatPrimitive, entity.floatPrimitive, "float");
		Assertions.assertEquals(this.doublePrimitive, entity.doublePrimitive, "double");
	}

}
