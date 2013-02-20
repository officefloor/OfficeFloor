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
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.office.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.EllipseFigure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;

/**
 * Standard {@link ExternalManagedObjectFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardExternalManagedObjectFigure extends
		AbstractOfficeFloorFigure implements ExternalManagedObjectFigure {

	/**
	 * {@link ExternalManagedObjectFigureContext}.
	 */
	private final ExternalManagedObjectFigureContext context;

	/**
	 * {@link Figure}.
	 */
	private final net.officefloor.eclipse.skin.standard.figure.EllipseFigure figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 */
	public StandardExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		this.context = context;
		this.figure = new EllipseFigure(
				this.context.getExternalManagedObjectName(),
				StandardOfficeFloorColours.EXTERNAL_OBJECT());
		this.setFigure(this.figure);
	}

	/*
	 * ================ ExternalManagedObjectFigure ==========================
	 */

	@Override
	public void setExternalManagedObjectName(String externalManagedObjectName) {
		this.figure.getLabel().setText(externalManagedObjectName);
	}

	@Override
	public IFigure getExternalManagedObjectNameFigure() {
		return this.figure.getLabel();
	}

}