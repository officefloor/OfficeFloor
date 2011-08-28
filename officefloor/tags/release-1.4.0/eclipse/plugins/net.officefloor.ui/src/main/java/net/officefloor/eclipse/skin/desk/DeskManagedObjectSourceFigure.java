/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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
import net.officefloor.model.desk.DeskManagedObjectSourceModel;

import org.eclipse.draw2d.IFigure;

/**
 * {@link OfficeFloorFigure} for the {@link DeskManagedObjectSourceModel}.
 *
 * @author Daniel Sagenschneider
 */
public interface DeskManagedObjectSourceFigure extends OfficeFloorFigure {

	/**
	 * Indicates change in name for the {@link DeskManagedObjectSourceModel}.
	 *
	 * @param deskManagedObjectSourceName
	 *            Name to display for {@link DeskManagedObjectSourceModel}.
	 */
	void setDeskManagedObjectName(String deskManagedObjectSourceName);

	/**
	 * <p>
	 * Obtains the {@link IFigure} containing the
	 * {@link DeskManagedObjectSourceModel} name.
	 * <p>
	 * This is to allow placement of the editor in changing the
	 * {@link DeskManagedObjectSourceModel} name.
	 *
	 * @return {@link IFigure} containing the
	 *         {@link DeskManagedObjectSourceModel} name.
	 */
	IFigure getDeskManagedObjectSourceNameFigure();

}