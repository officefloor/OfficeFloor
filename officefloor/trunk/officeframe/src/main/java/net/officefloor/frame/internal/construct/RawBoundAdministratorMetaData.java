/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.internal.structure.AdministratorIndex;
import net.officefloor.frame.internal.structure.AdministratorMetaData;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.TaskMetaData;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.Duty;

/**
 * Raw meta-data of a bound {@link Administrator}.
 * 
 * @author Daniel
 */
public interface RawBoundAdministratorMetaData<I, A extends Enum<A>> {

	/**
	 * Obtains the name the {@link Administrator} is bound under.
	 * 
	 * @return Name the {@link Administrator} is bound under.
	 */
	String getBoundAdministratorName();

	/**
	 * Obtains the {@link AdministratorIndex}.
	 * 
	 * @return {@link AdministratorIndex}.
	 */
	AdministratorIndex getAdministratorIndex();

	/**
	 * Obtains the keys identifying the {@link Duty} instances for the
	 * {@link Administrator}.
	 * 
	 * @return Keys identifying the {@link Duty} instances.
	 */
	A[] getDutyKeys();

	/**
	 * Obtains the {@link AdministratorMetaData} for this {@link Administrator}.
	 * 
	 * @return {@link AdministratorMetaData} for this {@link Administrator}.
	 */
	AdministratorMetaData<I, A> getAdministratorMetaData();

	/**
	 * Links the {@link TaskMetaData} instances to create {@link Flow} of
	 * execution.
	 * 
	 * @param taskMetaDataLocator
	 *            {@link OfficeMetaDataLocator}.
	 * @param assetManagerFactory
	 *            {@link AssetManagerFactory}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkTasks(OfficeMetaDataLocator taskMetaDataLocator,
			AssetManagerFactory assetManagerFactory, OfficeFloorIssues issues);

}