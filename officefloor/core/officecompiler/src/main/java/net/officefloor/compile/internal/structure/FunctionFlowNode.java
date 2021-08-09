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

import net.officefloor.compile.spi.section.FunctionFlow;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface FunctionFlowNode extends LinkFlowNode, FunctionFlow {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Function Flow";

	/**
	 * Initialises the {@link FunctionFlowNode}.
	 */
	void initialise();

	/**
	 * Indicates whether to spawn a {@link ThreadState} for this
	 * {@link FunctionFlow}.
	 * 
	 * @return <code>true</code> to spawn a {@link ThreadState} for this
	 *         {@link FunctionFlow}.
	 */
	boolean isSpawnThreadState();

	/**
	 * Specifies whether to spawn a {@link ThreadState} for this
	 * {@link FunctionFlow}.
	 * 
	 * @param isSpawnThreadState
	 *            <code>true</code> to spawn a {@link ThreadState} for this
	 *            {@link FunctionFlow}.
	 */
	void setSpawnThreadState(boolean isSpawnThreadState);

}
