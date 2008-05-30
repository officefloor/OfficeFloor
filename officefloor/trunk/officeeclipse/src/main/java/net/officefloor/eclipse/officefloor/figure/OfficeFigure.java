/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.eclipse.officefloor.figure;

import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.model.officefloor.OfficeFloorOfficeModel;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link Figure} for the {@link OfficeFloorOfficeModel}.
 * 
 * @author Daniel
 */
public class OfficeFigure extends Figure {

	/**
	 * Listing of teams.
	 */
	private final ListFigure teams;

	/**
	 * Listing of managed objects.
	 */
	private final ListFigure managedObjects;

	/**
	 * Listing of tasks.
	 */
	private final ListFigure tasks;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of the {@link OfficeFloorOfficeModel}.
	 */
	public OfficeFigure(String officeName) {
		this.setLayoutManager(new ToolbarLayout());
		this.setBackgroundColor(ColorConstants.lightGreen);
		this.setOpaque(true);
		this.setSize(60, 20);

		// Create the listing of figures
		this.teams = new ListFigure();
		this.managedObjects = new ListFigure();
		this.tasks = new ListFigure();

		// Decorate the office
		super.add(new ListItemFigure(officeName), null, -1);
		super.add(new IndentFigure(4, this.teams), null, -1);
		super.add(new IndentFigure(4, this.managedObjects), null, -1);
		super.add(new IndentFigure(4, this.tasks), null, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#add(org.eclipse.draw2d.IFigure,
	 *      java.lang.Object, int)
	 */
	@Override
	public void add(IFigure figure, Object constraint, int index) {
		if (figure instanceof OfficeTeamFigure) {
			this.teams.add(figure);
		} else if (figure instanceof OfficeManagedObjectFigure) {
			this.managedObjects.add(figure);
		} else if (figure instanceof OfficeTaskFigure) {
			this.tasks.add(figure);
		} else {
			throw new IllegalArgumentException("Unknown figure '"
					+ figure.getClass().getName() + "' for adding to "
					+ this.getClass().getName());
		}
	}

}
