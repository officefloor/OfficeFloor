/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.eclipse.skin.standard.desk;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigure;
import net.officefloor.eclipse.skin.desk.ExternalManagedObjectFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.ToolTipFigure;
import net.officefloor.model.desk.ExternalManagedObjectModel;

/**
 * Standard {@link ExternalManagedObjectFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardExternalManagedObjectFigure extends
		AbstractOfficeFloorFigure implements ExternalManagedObjectFigure {

	/**
	 * {@link Label} containing the {@link ExternalManagedObjectModel} name.
	 */
	private final Label externalManagedObjectName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link ExternalManagedObjectFigureContext}.
	 */
	public StandardExternalManagedObjectFigure(
			ExternalManagedObjectFigureContext context) {
		net.officefloor.eclipse.skin.standard.figure.ExternalManagedObjectFigure mo = new net.officefloor.eclipse.skin.standard.figure.ExternalManagedObjectFigure(
				context.getExternalManagedObjectName());
		this.externalManagedObjectName = mo.getLabel();

		// Provide tool tip
		mo
				.setToolTip(new ToolTipFigure(
						"External Managed Object\n\nObject type: "
								+ context.getObjectTypeName()
								+ "\n\nAllows linking to a Managed Object not within this Desk."));

		this.setFigure(mo);
	}

	/*
	 * ================ ExternalManagedObjectFigure =======================
	 */

	@Override
	public void setExternalManagedObjectName(String externalManagedObjectName) {
		this.externalManagedObjectName.setText(externalManagedObjectName);
	}

	@Override
	public IFigure getExternalManagedObjectNameFigure() {
		return this.externalManagedObjectName;
	}

}