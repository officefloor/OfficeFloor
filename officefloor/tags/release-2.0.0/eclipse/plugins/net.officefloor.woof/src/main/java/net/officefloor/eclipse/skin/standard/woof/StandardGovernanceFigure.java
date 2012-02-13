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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.StandardWoofColours;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.woof.GovernanceFigure;
import net.officefloor.eclipse.skin.woof.GovernanceFigureContext;

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Triangle;

/**
 * Standard {@link GovernanceFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardGovernanceFigure extends AbstractOfficeFloorFigure
		implements GovernanceFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link GovernanceFigureContext}.
	 */
	public StandardGovernanceFigure(GovernanceFigureContext context) {

		// Governance figure
		Triangle triangle = new Triangle();
		triangle.setBackgroundColor(StandardWoofColours.GOVERNANCE());
		triangle.setOutline(false);
		triangle.setOpaque(true);
		triangle.setDirection(Triangle.SOUTH);
		triangle.setLayoutManager(new NoSpacingGridLayout(1));

		// Add name
		Label name = new Label(context.getGovernanceName());
		name.setLayoutManager(new NoSpacingGridLayout(1));
		triangle.add(name);

		this.setFigure(triangle);
	}

}