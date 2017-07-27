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

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.AdministrationModel;

/**
 * {@link Figure} for the {@link AdministrationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change in name of the {@link AdministrationModel}.
	 * 
	 * @param administrationName
	 *            Name to display for the {@link AdministrationModel}.
	 */
	void setAdministrationName(String administrationName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link AdministrationModel}
	 * name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link AdministrationModel} name.
	 * 
	 * @return {@link IFigure} containing the {@link AdministrationModel} name.
	 */
	IFigure getAdministrationNameFigure();

}