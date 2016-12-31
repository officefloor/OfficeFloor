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

import java.util.Map;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.configuration.InputManagedObjectConfiguration;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectExecuteContextFactory;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Meta-data of a {@link ManagedObject} that is managed by the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawManagingOfficeMetaData<F extends Enum<F>> {

	/**
	 * Obtains the name for the {@link Office} managing the
	 * {@link ManagedObject}.
	 * 
	 * @return Name for the {@link Office} managing the {@link ManagedObject}.
	 */
	String getManagingOfficeName();

	/**
	 * <p>
	 * Indicates if the {@link ManagedObjectSource} requires instigating
	 * {@link Flow} instances.
	 * <p>
	 * If <code>true</code> it means the {@link ManagedObjectSource} must be
	 * bound to the {@link ProcessState} of the {@link Office}.
	 * 
	 * @return <code>true</code> if the {@link ManagedObjectSource} requires
	 *         instigating {@link Flow} instances.
	 */
	boolean isRequireFlows();

	/**
	 * <p>
	 * Obtains the {@link InputManagedObjectConfiguration} configuring the bind
	 * of the {@link ManagedObject} within the {@link ProcessState} of the
	 * {@link Office}.
	 * <p>
	 * Should the {@link ManagedObjectSource} instigate a {@link Flow}, a
	 * {@link ManagedObject} from the {@link ManagedObjectSource} is to be made
	 * available to the {@link ProcessState}. Whether the {@link Office} wants
	 * to make use of the {@link ManagedObject} is its choice but is available
	 * to do so.
	 * 
	 * @return {@link InputManagedObjectConfiguration} configuring the bind of
	 *         the {@link ManagedObject} within the {@link ProcessState} of the
	 *         {@link Office}.
	 */
	InputManagedObjectConfiguration<?> getInputManagedObjectConfiguration();

	/**
	 * Obtains the {@link RawManagedObjectMetaData} for the
	 * {@link ManagedObject} to be managed by the {@link Office}.
	 * 
	 * @return {@link RawManagedObjectMetaData} for the {@link ManagedObject} to
	 *         be managed by the {@link Office}.
	 */
	RawManagedObjectMetaData<?, F> getRawManagedObjectMetaData();

	/**
	 * Sets up the {@link ManagedObjectSource} to be managed by the
	 * {@link Office} of the input {@link ManagedFunctionLocator}.
	 * 
	 * @param processBoundManagedObjectMetaData
	 *            {@link RawBoundManagedObjectMetaData} of the
	 *            {@link ProcessState} bound {@link ManagedObject} instances of
	 *            the managing {@link Office}.
	 * @param officeMetaData
	 *            {@link OfficeMetaData}.
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator} for the {@link Office} managing
	 *            the {@link ManagedObjectSource}.
	 * @param officeTeams
	 *            {@link TeamManagement} instances by their {@link Office}
	 *            names.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void manageByOffice(RawBoundManagedObjectMetaData[] processBoundManagedObjectMetaData,
			OfficeMetaData officeMetaData, ManagedFunctionLocator functionLocator,
			Map<String, TeamManagement> officeTeams, OfficeFloorIssues issues);

	/**
	 * Obtains the {@link ManagedObjectExecuteContextFactory} for the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectExecuteContextFactory} for the
	 *         {@link ManagedObjectSource}.
	 */
	ManagedObjectExecuteContextFactory<F> getManagedObjectExecuteContextFactory();

}