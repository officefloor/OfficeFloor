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

import net.officefloor.compile.officefloor.OfficeFloorInputType;
import net.officefloor.compile.spi.officefloor.OfficeFloorInput;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Input to the {@link OfficeFloor}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorInputNode extends OfficeFloorInput {

	/**
	 * Obtains the {@link OfficeFloorInputType} for this
	 * {@link OfficeFloorInputNode}.
	 * 
	 * @return {@link OfficeFloorInputType}.
	 */
	OfficeFloorInputType getOfficeFloorInputType();

}