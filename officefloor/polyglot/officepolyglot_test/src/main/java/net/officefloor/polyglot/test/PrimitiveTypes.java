/*-
 * #%L
 * Polyglot Test
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

package net.officefloor.polyglot.test;

import lombok.Value;

/**
 * Primitive types.
 * 
 * @author Daniel Sagenschneider
 */
@Value
public class PrimitiveTypes {

	private boolean primitiveBoolean;

	private byte primitiveByte;

	private short primitiveShort;

	private char primitiveChar;

	private int primitiveInt;

	private long primitiveLong;

	private float primitiveFloat;

	private double primitiveDouble;

}
