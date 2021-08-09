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

package net.officefloor.compile.internal.structure;

/**
 * Auto-wiring of a source {@link Node} to target {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWireLink<S extends Node, T extends Node> {

	/**
	 * Obtains the source {@link Node}.
	 * 
	 * @return Source {@link Node}.
	 */
	S getSourceNode();

	/**
	 * Obtains the matching source {@link AutoWire}.
	 * 
	 * @return Matching source {@link AutoWire}.
	 */
	AutoWire getSourceAutoWire();

	/**
	 * Obtains the target {@link Node}.
	 * 
	 * @param office {@link OfficeNode}.
	 * @return Target {@link Node}.
	 */
	T getTargetNode(OfficeNode office);

	/**
	 * Obtains the matching target {@link AutoWire}.
	 * 
	 * @return Matching target {@link AutoWire}.
	 */
	AutoWire getTargetAutoWire();

}
