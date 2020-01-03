package net.officefloor.polyglot.test;

import lombok.Value;

/**
 * Object types.
 * 
 * @author Daniel Sagenschneider
 */
@Value
public class ObjectTypes {

	private final String string;

	private final JavaObject object;

	private final int[] primitiveArray;

	private final JavaObject[] objectArray;

}