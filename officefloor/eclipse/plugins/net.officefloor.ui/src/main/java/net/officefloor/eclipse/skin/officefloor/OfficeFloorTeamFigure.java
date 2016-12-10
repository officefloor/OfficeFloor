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
package net.officefloor.eclipse.skin.officefloor;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.officefloor.OfficeFloorTeamModel;

import org.eclipse.draw2d.IFigure;

/**
 * {@link OfficeFloorFigure} for the {@link OfficeFloorTeamModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorTeamFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change in name for the {@link OfficeFloorTeamModel}.
	 * 
	 * @param officeFloorTeamName
	 *            Name to display for the {@link OfficeFloorTeamModel}.
	 */
	void setOfficeFloorTeamName(String officeFloorTeamName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link OfficeFloorTeamModel}
	 * name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link OfficeFloorTeamModel} name.
	 * 
	 * @return {@link IFigure} containing the {@link OfficeFloorTeamModel} name.
	 */
	IFigure getOfficeFloorTeamNameFigure();

}