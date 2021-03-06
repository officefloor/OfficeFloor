/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
