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

import net.officefloor.eclipse.skin.office.OfficeSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSectionFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.model.office.OfficeSectionModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

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
		RoundedContainerFigure container = new RoundedContainerFigure(context
				.getOfficeSectionName(), StandardOfficeFloorColours.SECTION(), 20, false);
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