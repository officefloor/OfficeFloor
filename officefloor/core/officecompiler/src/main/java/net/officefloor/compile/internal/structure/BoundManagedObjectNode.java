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

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.thread.OptionalThreadLocal;

/**
 * {@link ManagedObject} bound into the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface BoundManagedObjectNode extends LinkObjectNode {

	/**
	 * Obtains the name under which this {@link ManagedObject} is made available to
	 * the {@link Office}.
	 * 
	 * @return Name under which this {@link ManagedObject} is made available to the
	 *         {@link Office}.
	 */
	String getBoundManagedObjectName();

	/**
	 * Obtains the {@link ManagedObjectSourceNode} for this
	 * {@link BoundManagedObjectNode}.
	 * 
	 * @return {@link ManagedObjectSourceNode} for this
	 *         {@link BoundManagedObjectNode}.
	 */
	ManagedObjectSourceNode getManagedObjectSourceNode();

	/**
	 * Adds the {@link GovernanceNode} to provide {@link Governance} over this
	 * {@link ManagedObjectNode} when used within the {@link Office} of the
	 * {@link OfficeNode}.
	 * 
	 * @param governance {@link GovernanceNode}.
	 * @param office     {@link OfficeNode} for which the {@link Governance} is
	 *                   applicable.
	 */
	void addGovernance(GovernanceNode governance, OfficeNode office);

	/**
	 * Adds the {@link AdministrationNode} to provide pre-load
	 * {@link Administration} for this {@link ManagedObjectNode} when used within
	 * the {@link Office} of the {@link OfficeNode}.
	 * 
	 * @param preLoadAdministration Pre-load {@link AdministrationNode}.
	 * @param office                {@link OfficeNode} for which the pre-load
	 *                              {@link Administration} is applicable.
	 */
	void addPreLoadAdministration(AdministrationNode preLoadAdministration, OfficeNode office);

	/**
	 * Builds the {@link ManagedObject} into the {@link Office}.
	 * 
	 * @param office         {@link OfficeNode} of the {@link Office} that this
	 *                       {@link ManagedObject} is to build itself into.
	 * @param officeBuilder  {@link OfficeBuilder} for the {@link Office}.
	 * @param officeBindings {@link OfficeBindings}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildOfficeManagedObject(OfficeNode office, OfficeBuilder officeBuilder, OfficeBindings officeBindings,
			CompileContext compileContext);

	/**
	 * Builds the {@link SupplierThreadLocal} from the bound {@link ManagedObject}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver} to
	 *                                    receive the {@link OptionalThreadLocal}.
	 */
	void buildSupplierThreadLocal(OptionalThreadLocalReceiver optionalThreadLocalReceiver);

}
