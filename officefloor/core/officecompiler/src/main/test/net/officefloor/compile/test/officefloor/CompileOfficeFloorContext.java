/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.compile.test.officefloor;

import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;

/**
 * Provides context for the {@link CompileOfficeFloorExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public interface CompileOfficeFloorContext {

	/**
	 * Obtains the {@link OfficeFloorDeployer}.
	 * 
	 * @return {@link OfficeFloorDeployer}.
	 */
	OfficeFloorDeployer getOfficeFloorDeployer();

	/**
	 * Obtains the {@link DeployedOffice}.
	 * 
	 * @return {@link DeployedOffice}.
	 */
	DeployedOffice getDeployedOffice();

	/**
	 * Obtains the {@link OfficeFloorSourceContext}.
	 * 
	 * @return {@link OfficeFloorSourceContext}.
	 */
	OfficeFloorSourceContext getOfficeFloorSourceContext();

}