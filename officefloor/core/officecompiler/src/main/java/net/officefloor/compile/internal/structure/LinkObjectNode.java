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
 * {@link LinkObjectNode} that can be linked to another {@link LinkObjectNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkObjectNode extends Node {

	/**
	 * Links the input {@link LinkObjectNode} to this {@link LinkObjectNode}.
	 * 
	 * @param node
	 *            {@link LinkObjectNode} to link to this {@link LinkObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkObjectNode(LinkObjectNode node);

	/**
	 * Obtains the {@link LinkObjectNode} linked to this {@link LinkObjectNode}.
	 * 
	 * @return {@link LinkObjectNode} linked to this {@link LinkObjectNode}.
	 */
	LinkObjectNode getLinkedObjectNode();

}
