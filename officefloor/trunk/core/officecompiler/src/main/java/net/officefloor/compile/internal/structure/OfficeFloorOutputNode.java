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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.officefloor.OfficeFloorOutputType;
import net.officefloor.compile.spi.officefloor.OfficeFloorOutput;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Output from the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorOutputNode extends OfficeFloorOutput {

	/**
	 * Obtains the {@link OfficeFloorOutputType} for this
	 * {@link OfficeFloorOutputNode}.
	 * 
	 * @return {@link OfficeFloorOutputType}.
	 */
	OfficeFloorOutputType getOfficeFloorOutputType();

}