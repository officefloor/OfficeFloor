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
 * {@link LinkOfficeNode} that can be linked to another {@link LinkOfficeNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkOfficeNode extends Node {

	/**
	 * Links the input {@link LinkOfficeNode} to this {@link LinkOfficeNode}.
	 * 
	 * @param node
	 *            {@link LinkOfficeNode} to link to this {@link LinkOfficeNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkOfficeNode(LinkOfficeNode node);

	/**
	 * Obtains the {@link LinkOfficeNode} linked to this {@link LinkOfficeNode}.
	 * 
	 * @return {@link LinkOfficeNode} linked to this {@link LinkOfficeNode}.
	 */
	LinkOfficeNode getLinkedOfficeNode();

}
