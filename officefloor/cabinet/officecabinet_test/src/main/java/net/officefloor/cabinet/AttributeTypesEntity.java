/*-
 * #%L
 * OfficeFloor Filing Cabinet Test
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
