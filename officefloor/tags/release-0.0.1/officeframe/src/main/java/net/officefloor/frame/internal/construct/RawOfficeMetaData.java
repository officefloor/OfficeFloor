/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Team;

/**
 * Raw meta-data of the {@link Office}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawOfficeMetaData {

	/**
	 * Name of the {@link Office}.
	 */
	String getOfficeName();

	/**
	 * Obtains {@link RawOfficeFloorMetaData} containing this {@link Office}.
	 * 
	 * @return {@link RawOfficeFloorMetaData}.
	 */
	RawOfficeFloorMetaData getRawOfficeFloorMetaData();

	/**
	 * Obtains the {@link Team} instances by their {@link Office} registered
	 * names.
	 * 
	 * @return {@link Team} instances by their {@link Office} registered names.
	 */
	Map<String, Team> getTeams();

	/**
	 * Obtains the {@link RawManagedObjectMetaData} by their {@link Office}
	 * registered names.
	 * 
	 * @return {@link RawManagedObjectMetaData} by their {@link Office}
	 *         registered names.
	 */
	Map<String, RawManagedObjectMetaData<?, ?>> getManagedObjectMetaData();

	/**
	 * Obtains the {@link ProcessState} {@link RawBoundManagedObjectMetaData}
	 * instances.
	 * 
	 * @return {@link ProcessState} {@link RawBoundManagedObjectMetaData}
	 *         instances.
	 */
	RawBoundManagedObjectMetaData<?>[] getProcessBoundManagedObjects();

	/**
	 * Obtains the {@link ThreadState} {@link RawBoundManagedObjectMetaData}
	 * instances.
	 * 
	 * @return {@link ThreadState} {@link RawBoundManagedObjectMetaData}
	 *         instances.
	 */
	RawBoundManagedObjectMetaData<?>[] getThreadBoundManagedObjects();

	/**
	 * Obtains the scope {@link RawBoundManagedObjectMetaData} instances of the
	 * {@link Office} by the {@link ProcessState} and {@link ThreadState} bound
	 * names.
	 * 
	 * @return Scope {@link RawBoundManagedObjectMetaData} instances of the
	 *         {@link Office} by the {@link ProcessState} and
	 *         {@link ThreadState} bound names.
	 */
	Map<String, RawBoundManagedObjectMetaData<?>> getOfficeScopeManagedObjects();

	/**
	 * Obtains the {@link ProcessState} {@link RawBoundAdministratorMetaData}
	 * instances.
	 * 
	 * @return {@link ProcessState} {@link RawBoundAdministratorMetaData}
	 *         instances.
	 */
	RawBoundAdministratorMetaData<?, ?>[] getProcessBoundAdministrators();

	/**
	 * Obtains the {@link ThreadState} {@link RawBoundAdministratorMetaData}
	 * instances.
	 * 
	 * @return {@link ThreadState} {@link RawBoundAdministratorMetaData}
	 *         instances.
	 */
	RawBoundAdministratorMetaData<?, ?>[] getThreadBoundAdministrators();

	/**
	 * Obtains the scope {@link RawBoundAdministratorMetaData} instances of the
	 * {@link Office} by their {@link ProcessState} and {@link ThreadState}
	 * bound names.
	 * 
	 * @return Scope {@link RawBoundAdministratorMetaData} instances of the
	 *         {@link Office} by the {@link ProcessState} and
	 *         {@link ThreadState} bound names.
	 */
	Map<String, RawBoundAdministratorMetaData<?, ?>> getOfficeScopeAdministrators();

	/**
	 * Obtains the {@link OfficeMetaData}.
	 * 
	 * @return {@link OfficeMetaData}.
	 */
	OfficeMetaData getOfficeMetaData();

}