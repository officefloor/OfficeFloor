/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.eclipse.skin.standard.officefloor;

import net.officefloor.eclipse.skin.officefloor.OfficeFloorInputManagedObjectFigure;
import net.officefloor.eclipse.skin.officefloor.OfficeFloorInputManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link OfficeFloorInputManagedObjectFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardOfficeFloorInputManagedObjectFigure extends
		AbstractOfficeFloorFigure implements
		OfficeFloorInputManagedObjectFigure {

	/**
	 * {@link OfficeFloorInputManagedObjectFigureContext}.
	 */
	private final OfficeFloorInputManagedObjectFigureContext context;

	/**
	 * {@link IFigure} containing the name.
	 */
	private Label name;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link OfficeFloorInputManagedObjectFigureContext}.
	 */
	public StandardOfficeFloorInputManagedObjectFigure(
			OfficeFloorInputManagedObjectFigureContext context) {
		this.context = context;

		// TODO provide figure
		this.name = new Label(this.context
				.getOfficeFloorInputManagedObjectName());

		// Specify the figures
		this.setFigure(this.name);
	}

	/*
	 * ============= OfficeFloorInputManagedObjectFigure ==================
	 */

	@Override
	public void setOfficeFloorInputManagedObjectName(
			String officeFloorInputManagedObjectName) {
		this.name.setText(officeFloorInputManagedObjectName);
	}

	@Override
	public IFigure getOfficeFloorInputManagedObjectNameFigure() {
		return this.name;
	}

}