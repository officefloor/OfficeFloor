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

import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Node representing an instance use of an Input {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface InputManagedObjectNode extends LinkObjectNode, BoundManagedObjectNode, OfficeFloorInputManagedObject {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Input Managed Object";

	/**
	 * Initialises the {@link InputManagedObjectNode}.
	 */
	void initialise();

	/**
	 * Obtains the input object type.
	 * 
	 * @return Input object type.
	 */
	String getInputObjectType();

	/**
	 * Obtains the bound {@link ManagedObjectSourceNode} for this
	 * {@link InputManagedObjectNode}.
	 * 
	 * @return Bound {@link ManagedObjectSourceNode} for this
	 *         {@link InputManagedObjectNode}.
	 */
	ManagedObjectSourceNode getBoundManagedObjectSourceNode();

	/**
	 * Obtains the {@link GovernanceNode} instances providing {@link Governance}
	 * over this {@link InputManagedObjectNode}.
	 * 
	 * @param managingOffice
	 *            {@link OfficeNode} managing the {@link InputManagedObjectNode}
	 *            , which ensures that {@link Governance} does not extend beyond
	 *            the particular {@link OfficeNode}.
	 * @return {@link GovernanceNode} instances providing {@link Governance}
	 *         over this {@link InputManagedObjectNode}.
	 */
	GovernanceNode[] getGovernances(OfficeNode managingOffice);

	/**
	 * Obtains the pre-load {@link AdministrationNode} instances providing
	 * {@link Administration} over this {@link InputManagedObjectNode}.
	 * 
	 * @param managingOffice
	 *            {@link OfficeNode} managing the
	 *            {@link InputManagedObjectNode}, which ensures that
	 *            {@link Administration} does not extend beyond the particular
	 *            {@link OfficeNode}.
	 * @return {@link AdministrationNode} instances providing pre-load
	 *         {@link Administration} over this {@link InputManagedObjectNode}.
	 */
	AdministrationNode[] getPreLoadAdministrations(OfficeNode managingOffice);

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link InputManagedObjectNode}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link TypeQualification} instances for the
	 *         {@link InputManagedObjectNode}.
	 */
	TypeQualification[] getTypeQualifications(CompileContext compileContext);

}
