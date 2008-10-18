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
package net.officefloor.eclipse.skin.standard.room;

import net.officefloor.eclipse.common.figure.ListFigure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link org.eclipse.draw2d.Figure} for the
 * {@link net.officefloor.model.room.SubRoomModel}.
 * 
 * @author Daniel
 */
public class SubRoomFigure extends Figure {

	/**
	 * Listing of input flows.
	 */
	private ListFigure inputFlows;

	/**
	 * Listing of managed objects.
	 */
	private ListFigure managedObjects;

	/**
	 * Listing of output flows.
	 */
	private ListFigure outputFlows;

	/**
	 * Listing of escalations.
	 */
	private ListFigure escalations;

	/**
	 * Initiate.
	 * 
	 * @param subRoomId
	 *            Id of the {@link net.officefloor.model.room.SubRoomModel}.
	 */
	public SubRoomFigure(String subRoomId) {
		this.setLayoutManager(new ToolbarLayout());
		this.setBackgroundColor(ColorConstants.lightGreen);
		this.setOpaque(true);
		this.setSize(60, 20);

		// Create the listing of figures
		this.inputFlows = new ListFigure();
		this.managedObjects = new ListFigure();
		this.outputFlows = new ListFigure();
		this.escalations = new ListFigure();

		// Decorate the sub room
		super.add(new Label(subRoomId), null, -1);
		super.add(this.inputFlows, null, -1);
		super.add(this.managedObjects, null, -1);
		super.add(this.outputFlows, null, -1);
		super.add(this.escalations, null, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.Figure#add(org.eclipse.draw2d.IFigure,
	 *      java.lang.Object, int)
	 */
	@Override
	public void add(IFigure figure, Object constraint, int index) {
		if (figure instanceof SubRoomInputFlowFigure) {
			this.inputFlows.add(figure);
		} else if (figure instanceof SubRoomManagedObjectFigure) {
			this.managedObjects.add(figure);
		} else if (figure instanceof SubRoomOutputFlowFigure) {
			this.outputFlows.add(figure);
		} else if (figure instanceof SubRoomEscalationFigure) {
			this.escalations.add(figure);
		} else {
			throw new IllegalArgumentException("Unknown figure '"
					+ figure.getClass().getName() + "' for adding to "
					+ this.getClass().getName());
		}
	}
}
