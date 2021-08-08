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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link OfficeObject} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeObjectNode extends LinkObjectNode, OfficeObject {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Object";

	/**
	 * Initialises this {@link OfficeManagedObjectType}.
	 * 
	 * @param objectType
	 *            Object type.
	 */
	void initialise(String objectType);

	/**
	 * <p>
	 * Adds an {@link AdministrationNode} for this
	 * {@link OfficeManagedObjectType}.
	 * <p>
	 * This allows the {@link OfficeManagedObjectType} to report the extension
	 * types required to be supported by the {@link OfficeFloorManagedObject}
	 * for the {@link OfficeObject}.
	 * 
	 * @param administrator
	 *            {@link AdministrationNode}.
	 */
	void addAdministrator(AdministrationNode administrator);

	/**
	 * Obtains the {@link AdministrationNode} instances to provide pre-load
	 * {@link Administration} over {@link BoundManagedObjectNode} linked to this
	 * {@link OfficeObjectNode}.
	 * 
	 * @return {@link AdministrationNode} instances.
	 */
	AdministrationNode[] getPreLoadAdministrations();

	/**
	 * <p>
	 * Adds a {@link GovernanceNode} providing {@link Governance} for this
	 * {@link OfficeObject}.
	 * <p>
	 * This also allows the {@link OfficeManagedObjectType} to report the
	 * extension interfaces required to be supported by the
	 * {@link OfficeFloorManagedObject} for the {@link OfficeObject}.
	 * 
	 * @param governance
	 *            {@link GovernanceNode}.
	 */
	void addGovernance(GovernanceNode governance);

	/**
	 * Obtains the {@link GovernanceNode} instances to provide
	 * {@link Governance} over {@link BoundManagedObjectNode} linked to this
	 * {@link OfficeObjectNode}.
	 * 
	 * @return {@link GovernanceNode} instances.
	 */
	GovernanceNode[] getGovernances();

	/**
	 * Obtains the type of the {@link OfficeObject}.
	 * 
	 * @return Type of the {@link OfficeObject}.
	 */
	String getOfficeObjectType();

	/**
	 * Obtains the type qualifier for the {@link OfficeObject}.
	 * 
	 * @return Type qualifier for the {@link OfficeObject}.
	 */
	String getTypeQualifier();

	/**
	 * Loads the {@link OfficeManagedObjectType} for this
	 * {@link OfficeObjectNode}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeManagedObjectType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeManagedObjectType loadOfficeManagedObjectType(CompileContext compileContext);

}
