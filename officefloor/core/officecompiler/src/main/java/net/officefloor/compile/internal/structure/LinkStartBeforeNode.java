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
 * {@link LinkStartBeforeNode} that can be linked to another
 * {@link LinkStartBeforeNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface LinkStartBeforeNode extends Node {

	/**
	 * Links this {@link LinkStartBeforeNode} to start before the input
	 * {@link LinkStartBeforeNode}.
	 * 
	 * @param node {@link LinkStartBeforeNode} to have this
	 *             {@link LinkStartBeforeNode} start before.
	 * @return <code>true</code> if linked.
	 */
	boolean linkStartBeforeNode(LinkStartBeforeNode node);

	/**
	 * Obtains the {@link LinkStartBeforeNode} instances.
	 * 
	 * @return {@link LinkStartBeforeNode} instances.
	 */
	LinkStartBeforeNode[] getLinkedStartBeforeNodes();

}
