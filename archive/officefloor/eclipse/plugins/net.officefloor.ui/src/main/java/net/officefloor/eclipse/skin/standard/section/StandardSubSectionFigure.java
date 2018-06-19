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
package net.officefloor.eclipse.skin.standard.section;

import net.officefloor.eclipse.skin.section.SubSectionFigure;
import net.officefloor.eclipse.skin.section.SubSectionFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.model.section.SubSectionModel;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;

/**
 * Standard {@link SubSectionFigure}.
 *
 * @author Daniel Sagenschneider
 */
public class StandardSubSectionFigure extends AbstractOfficeFloorFigure
		implements SubSectionFigure {

	/**
	 * {@link Label} for the {@link SubSectionModel} name.
	 */
	private final Label subSectionName;

	/**
	 * Initiate.
	 *
	 * @param context
	 *            {@link SubSectionFigureContext}.
	 */
	public StandardSubSectionFigure(SubSectionFigureContext context) {

		// Create the container for the sub section
		RoundedContainerFigure subSectionFigure = new RoundedContainerFigure(context
				.getSubSectionName(), StandardOfficeFloorColours.SUB_SECTION(), 5, false);
		this.subSectionName = subSectionFigure.getContainerName();

		// Specify the figures
		this.setFigure(subSectionFigure);
		this.setContentPane(subSectionFigure.getContentPane());
	}

	/*
	 * =================== SubSectionFigure =============================
	 */

	@Override
	public void setSubSectionName(String subSectionName) {
		this.subSectionName.setText(subSectionName);
	}

	@Override
	public IFigure getSubSectionNameFigure() {
		return this.subSectionName;
	}

}