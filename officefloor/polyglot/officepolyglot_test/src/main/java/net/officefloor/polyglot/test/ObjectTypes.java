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
package net.officefloor.polyglot.test;

/**
 * Object types.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectTypes {

	private final String string;

	private final JavaObject object;

	private final int[] primitiveArray;

	private final JavaObject[] objectArray;

	public ObjectTypes(String string, JavaObject object, int[] primitiveArray, JavaObject[] objectArray) {
		this.string = string;
		this.object = object;
		this.primitiveArray = primitiveArray;
		this.objectArray = objectArray;
	}

	public String getString() {
		return string;
	}

	public JavaObject getObject() {
		return object;
	}

	public int[] getPrimitiveArray() {
		return primitiveArray;
	}

	public JavaObject[] getObjectArray() {
		return objectArray;
	}

}