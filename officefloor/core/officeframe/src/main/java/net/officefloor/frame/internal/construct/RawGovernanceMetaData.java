/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Raw meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawGovernanceMetaData<E, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link Governance}.
	 * 
	 * @return Name of the {@link Governance}.
	 */
	String getGovernanceName();

	/**
	 * Obtains the extension interface type used by the {@link Governance}.
	 * 
	 * @return Extension interface type used by the {@link Governance}.
	 */
	Class<E> getExtensionInterfaceType();

	/**
	 * Obtains the index to obtain the {@link Governance} from the
	 * {@link ProcessState}.
	 * 
	 * @return Index to obtain the {@link Governance} from the
	 *         {@link ProcessState}.
	 */
	int getGovernanceIndex();

	/**
	 * Links the {@link ManagedFunctionMetaData} instances to enable
	 * {@link Flow} of execution.
	 * 
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void loadOfficeMetaData(OfficeMetaData officeMetaData, OfficeFloorIssues issues);

	/**
	 * Obtains the {@link GovernanceMetaData}.
	 * 
	 * @return {@link GovernanceMetaData}.
	 */
	GovernanceMetaData<E, F> getGovernanceMetaData();

}