package net.officefloor.cabinet;

import org.junit.jupiter.api.Assertions;

/**
 * Entity to ensure all attribute types.
 * 
 * @author Daniel Sagenschneider
 */
public class AttributeTypesEntity {

	private boolean booleanPrimitive;

	private byte bytePrimitive;

	private short shortPrimitive;

	private char charPrimitive;

	private int intPrimitive;

	private long longPrimitive;

	private float floatPrimitive;

	private double doublePrimitive;

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