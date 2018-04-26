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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigure;
import net.officefloor.eclipse.skin.woof.GovernanceAreaFigureContext;
import net.officefloor.model.woof.WoofGovernanceAreaModel;

import org.eclipse.draw2d.RoundedRectangle;

/**
 * Standard {@link GovernanceAreaFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardGovernanceAreaFigure extends AbstractOfficeFloorFigure
		implements GovernanceAreaFigure {

	/**
	 * {@link RoundedRectangle} for {@link WoofGovernanceAreaModel}.
	 */
	private final RoundedRectangle figure;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link GovernanceAreaFigureContext}.
	 */
	public StandardGovernanceAreaFigure(GovernanceAreaFigureContext context) {

		// Governance area figure
		this.figure = new RoundedRectangle();
		this.figure.setBackgroundColor(CommonWoofColours.GOVERNANCE());
		this.figure.setSize(context.getWidth(), context.getHeight());

		// Provide transparency to see overlapping governance areas
		this.figure.setOpaque(false);
		this.figure.setAlpha(100);

		// Specify the figure
		this.setFigure(this.figure);
	}

	/*
	 * =================== GovernanceAreaFigure ============================
	 */

	@Override
	public void resize(int width, int height) {
		this.figure.setSize(width, height);
	}

}