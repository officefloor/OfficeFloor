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
import net.officefloor.frame.internal.structure.JobSequence;
import net.officefloor.frame.internal.structure.GovernanceMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.governance.Governance;

/**
 * Raw meta-data of the {@link Governance}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawGovernanceMetaData<I, F extends Enum<F>> {

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
	Class<I> getExtensionInterfaceType();

	/**
	 * Obtains the index to obtain the {@link Governance} from the
	 * {@link ProcessState}.
	 * 
	 * @return Index to obtain the {@link Governance} from the
	 *         {@link ProcessState}.
	 */
	int getGovernanceIndex();

	/**
	 * Links the {@link TaskMetaData} instances to enable {@link JobSequence} of
	 * execution.
	 * 
	 * @param taskLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkOfficeMetaData(OfficeMetaDataLocator taskLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

	/**
	 * Obtains the {@link GovernanceMetaData}.
	 * 
	 * @return {@link GovernanceMetaData}.
	 */
	GovernanceMetaData<I, F> getGovernanceMetaData();

}