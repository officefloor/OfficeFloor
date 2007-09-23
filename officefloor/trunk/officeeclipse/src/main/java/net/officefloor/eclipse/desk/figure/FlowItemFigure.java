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
package net.officefloor.eclipse.desk.figure;

import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link org.eclipse.draw2d.Figure} for a
 * {@link net.officefloor.model.desk.FlowItemModel}
 * 
 * @author Daniel
 */
public class FlowItemFigure extends Figure {

	/**
	 * Outputs.
	 */
	private ListFigure outputs = new ListFigure();

	/**
	 * Initiate.
	 * 
	 * @param flowId
	 *            Id of flow.
	 * @param figureForIsPublic
	 *            {@link IFigure} to handle is public.
	 */
	public FlowItemFigure(String flowId, IFigure figureForIsPublic) {
		this.setLayoutManager(new ToolbarLayout());
		this.setBackgroundColor(ColorConstants.lightGreen);
		this.setOpaque(true);
		this.setSize(60, 20);

		// All adding
		super.add(new ListItemFigure(flowId), null, -1);
		super.add(figureForIsPublic, null, -1);
		super.add(new IndentFigure(10, this.outputs), null, -1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.draw2d.IFigure#add(org.eclipse.draw2d.IFigure,
	 *      java.lang.Object, int)
	 */
	@Override
	public void add(IFigure figure, Object constraint, int index) {
		// Add to outputs figure
		this.outputs.add(figure, constraint, index);
	}

}
