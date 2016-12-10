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

import net.officefloor.eclipse.skin.office.OfficeSubSectionFigure;
import net.officefloor.eclipse.skin.office.OfficeSubSectionFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.MarginBorder;

/**
 * {@link OfficeSubSectionFigure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardOfficeSubSectionFigure extends AbstractOfficeFloorFigure
		implements OfficeSubSectionFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link OfficeSubSectionFigureContext}.
	 */
	public StandardOfficeSubSectionFigure(OfficeSubSectionFigureContext context) {

		Figure figure = new Figure();
		figure.setLayoutManager(new NoSpacingToolbarLayout(true));

		// Content pane
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPane.setBorder(new MarginBorder(2, 4, 2, 2));
		figure.add(contentPane);

		// Specify the figure and content pane
		this.setFigure(figure);
		this.setContentPane(contentPane);
	}

	/*
	 * =================== OfficeSubSectionFigure ==================
	 */

	@Override
	public void setOfficeSubSectionName(String officeSubSectionName) {
		// Name not displayed
	}

}