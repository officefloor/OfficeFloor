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
package net.officefloor.eclipse.skin.desk;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.desk.WorkTaskObjectModel;

/**
 * {@link OfficeFloorFigure} for the {@link WorkTaskObjectFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WorkTaskObjectFigure extends OfficeFloorFigure {

	/**
	 * Specifies the {@link WorkTaskObjectModel} name to display.
	 * 
	 * @param context
	 *            {@link WorkTaskObjectFigureContext} to determine name to
	 *            display.
	 */
	void setWorkTaskObjectName(WorkTaskObjectFigureContext context);

	/**
	 * Flags to display whether a parameter.
	 * 
	 * @param isParameter
	 *            <code>true</code> if is a parameter.
	 */
	void setIsParameter(boolean isParameter);

}