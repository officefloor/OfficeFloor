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
 * {@link LinkFlowNode} that can be linked to another {@link LinkFlowNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkFlowNode extends Node {

	/**
	 * Links the input {@link LinkFlowNode} to this {@link LinkFlowNode}.
	 * 
	 * @param node
	 *            {@link LinkFlowNode} to link to this {@link LinkFlowNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkFlowNode(LinkFlowNode node);

	/**
	 * Obtains the {@link LinkFlowNode} linked to this {@link LinkFlowNode}.
	 * 
	 * @return {@link LinkFlowNode} linked to this {@link LinkFlowNode}.
	 */
	LinkFlowNode getLinkedFlowNode();

}
