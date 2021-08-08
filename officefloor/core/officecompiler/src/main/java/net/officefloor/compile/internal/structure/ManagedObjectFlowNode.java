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

import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObjectFlow;

/**
 * {@link ManagedObjectFlow} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectFlowNode extends LinkFlowNode, AugmentedManagedObjectFlow, SectionManagedObjectFlow,
		OfficeManagedObjectFlow, OfficeFloorManagedObjectFlow {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Source Flow";

	/**
	 * Initialises the {@link ManagedObjectFlowNode}.
	 */
	void initialise();

}
