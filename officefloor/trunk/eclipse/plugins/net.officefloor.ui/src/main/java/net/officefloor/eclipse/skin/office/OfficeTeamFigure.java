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
package net.officefloor.eclipse.skin.office;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.OfficeTeamModel;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;

/**
 * {@link Figure} for the {@link OfficeTeamModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change to the name of the {@link OfficeTeamModel}.
	 * 
	 * @param officeTeamName
	 *            Name to display for the {@link OfficeTeamModel}.
	 */
	void setOfficeTeamName(String officeTeamName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link OfficeTeamModel} name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link OfficeTeamModel} name.
	 * 
	 * @return {@link IFigure} containing the {@link OfficeTeamModel} name.
	 */
	IFigure getOfficeTeamNameFigure();
}