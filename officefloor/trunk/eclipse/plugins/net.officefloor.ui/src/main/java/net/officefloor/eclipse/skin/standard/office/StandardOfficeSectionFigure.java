/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.eclipse.skin.standard.office;

import net.officefloor.eclipse.skin.office.OfficeSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.model.office.OfficeSectionModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link OfficeSectionFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeSectionFigure extends AbstractOfficeFloorFigure
		implements OfficeSectionFigure {

	/**
	 * {@link Label} containing the {@link OfficeSectionModel} name.
	 */
	private final Label officeSectionName;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeSectionFigureContext}.
	 */
	public StandardOfficeSectionFigure(OfficeSectionFigureContext context) {

		Color flowColour = new Color(null, 130, 255, 150);

		RoundedContainerFigure container = new RoundedContainerFigure(context
				.getOfficeSectionName(), flowColour, 20, false);
		this.officeSectionName = container.getContainerName();

		// Specify figures
		this.setFigure(container);
		this.setContentPane(container.getContentPane());
	}

	/*
	 * ================== OfficeSectionFigure ==========================
	 */

	@Override
	public void setOfficeSectionName(String officeSectionName) {
		this.officeSectionName.setText(officeSectionName);
	}

	@Override
	public IFigure getOfficeSectionNameFigure() {
		return this.officeSectionName;
	}

}