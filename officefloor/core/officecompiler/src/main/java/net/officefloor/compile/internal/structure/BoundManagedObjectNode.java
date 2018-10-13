/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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