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
package net.officefloor.compile.spi.officefloor;

import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.frame.api.manage.FunctionManager;

/**
 * Input into a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public interface DeployedOfficeInput {

	/**
	 * Obtains the name of the {@link DeployedOfficeInput}.
	 * 
	 * @return Name of the {@link DeployedOfficeInput}.
	 */
	String getDeployedOfficeInputName();

	/**
	 * <p>
	 * Obtains the {@link FunctionManager} to externally trigger this
	 * {@link DeployedOfficeInput}.
	 * <p>
	 * This allows {@link OfficeFloorExtensionService} instances to obtain the
	 * {@link FunctionManager} for external triggering of service handling.
	 * 
	 * @return {@link FunctionManager} to externally trigger this
	 *         {@link DeployedOfficeInput}.
	 */
	FunctionManager getFunctionManager();

}