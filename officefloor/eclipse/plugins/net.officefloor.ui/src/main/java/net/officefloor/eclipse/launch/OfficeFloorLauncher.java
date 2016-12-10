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
package net.officefloor.eclipse.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.WorkManager;
import net.officefloor.model.office.OfficeModel;

/**
 * Launches the {@link OfficeFloor}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorLauncher {

	/**
	 * {@link ILaunchConfigurationType} identifier for running the
	 * {@link OfficeFloor}.
	 */
	public static final String ID_OFFICE_FLOOR_CONFIGURATION_TYPE = "net.officefloor.eclipse.launch.configurationtype.officeFloor";

	/**
	 * {@link ILaunchConfiguration} attribute to specify the {@link OfficeFloor}
	 * configuration file.
	 */
	public static final String ATTR_OFFICE_FLOOR_FILE = "net.officefloor.file";

	/**
	 * {@link ILaunchConfiguration} attribute to specify the name of the
	 * {@link OfficeModel} to invoke when launching.
	 */
	public static final String ATTR_OFFICE_NAME = "net.officefloor.office";

	/**
	 * {@link ILaunchConfiguration} attribute to specify the name of the
	 * {@link WorkManager} to invoke when launching.
	 */
	public static final String ATTR_WORK_NAME = "net.officefloor.work";
}
