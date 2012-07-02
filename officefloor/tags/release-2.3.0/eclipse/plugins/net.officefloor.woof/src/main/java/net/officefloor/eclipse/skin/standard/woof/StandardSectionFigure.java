/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.eclipse.skin.standard.woof;

import org.eclipse.draw2d.Label;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardWoofColours;
import net.officefloor.eclipse.skin.standard.figure.RoundedContainerFigure;
import net.officefloor.eclipse.skin.woof.SectionFigure;
import net.officefloor.eclipse.skin.woof.SectionFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;

/**
 * Standard {@link SectionFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSectionFigure extends AbstractOfficeFloorFigure implements
		SectionFigure {

	/**
	 * Name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link TemplateFigureContext}.
	 */
	public StandardSectionFigure(SectionFigureContext context) {

		// Create the container for the section
		RoundedContainerFigure figure = new RoundedContainerFigure(
				context.getSectionName(), StandardWoofColours.SECTION(), 5,
				false);
		this.name = figure.getContainerName();

		// Specify the figures
		this.setFigure(figure);
		this.setContentPane(figure.getContentPane());
	}

	/*
	 * ===================== SectionFigure ==========================
	 */

	@Override
	public void setSectionName(String sectionName) {
		this.name.setText(sectionName);
	}

}