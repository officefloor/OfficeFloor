/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.office;

import net.officefloor.eclipse.skin.OfficeFloorFigure;
import net.officefloor.model.office.AdministratorModel;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;

/**
 * {@link Figure} for the {@link AdministratorModel}.
 * 
 * @author Daniel
 */
public interface AdministratorFigure extends OfficeFloorFigure {

	/**
	 * Indicates a change in name of the {@link AdministratorModel}.
	 * 
	 * @param administratorName
	 *            Name to display for the {@link AdministratorModel}.
	 */
	void setAdministratorName(String administratorName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the {@link AdministratorModel}
	 * name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link AdministratorModel} name.
	 * 
	 * @return {@link IFigure} containing the {@link AdministratorModel} name.
	 */
	IFigure getAdministratorNameFigure();

}