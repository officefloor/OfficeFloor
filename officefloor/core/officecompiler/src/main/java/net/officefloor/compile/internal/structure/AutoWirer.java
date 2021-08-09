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

import java.util.function.Function;

/**
 * Auto wirer.
 * 
 * @author Daniel Sagenschneider
 */
public interface AutoWirer<N extends Node> {

	/**
	 * Adds an {@link AutoWire} target for selection.
	 * 
	 * @param targetNode      Target {@link Node}.
	 * @param targetAutoWires Target {@link AutoWire} instances supported by the
	 *                        {@link Node}.
	 */
	void addAutoWireTarget(N targetNode, AutoWire... targetAutoWires);

	/**
	 * Adds an {@link AutoWire} target for selection.
	 * 
	 * @param targetNodeFactory {@link Function} to create the target {@link Node}.
	 *                          This enables dynamically adding the target
	 *                          {@link Node} only if it is selected for linking. The
	 *                          {@link OfficeNode} is available to configure the
	 *                          {@link ManagingOfficeNode} for the
	 *                          {@link ManagedObjectSourceNode}.
	 * @param targetAutoWires   Target {@link AutoWire} instances supported by the
	 *                          {@link Node}.
	 */
	void addAutoWireTarget(Function<OfficeNode, ? extends N> targetNodeFactory, AutoWire... targetAutoWires);

	/**
	 * Selects the appropriate {@link AutoWireLink} instances.
	 * 
	 * @param                 <S> Source {@link Node} type.
	 * @param sourceNode      Source {@link Node} to link target.
	 * @param sourceAutoWires Source {@link AutoWire} instances to match against
	 *                        target {@link AutoWire} instances.
	 * @return Matching {@link AutoWireLink} instances.
	 */
	<S extends Node> AutoWireLink<S, N>[] getAutoWireLinks(S sourceNode, AutoWire... sourceAutoWires);

	/**
	 * Selects the appropriate {@link AutoWireLink} instances, and does not flag
	 * issue if no matching {@link AutoWireLink} is found.
	 * 
	 * @param                 <S> Source {@link Node} type.
	 * @param sourceNode      Source {@link Node} to link target.
	 * @param sourceAutoWires Source {@link AutoWire} instances to match against
	 *                        target {@link AutoWire} instances.
	 * @return Matching {@link AutoWireLink} instances.
	 */
	<S extends Node> AutoWireLink<S, N>[] findAutoWireLinks(S sourceNode, AutoWire... sourceAutoWires);

	/**
	 * <p>
	 * Creates an {@link AutoWirer} for a new scope that takes priority over
	 * existing {@link AutoWire} targets.
	 * <p>
	 * Targets are first looked for in the returned scoped {@link AutoWirer}. If no
	 * matching target is found, then this {@link AutoWire} is checked.
	 * 
	 * @return Scoped {@link AutoWirer}.
	 */
	AutoWirer<N> createScopeAutoWirer();

}
