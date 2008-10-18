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
package net.officefloor.eclipse.skin.standard.desk;

import net.officefloor.eclipse.common.figure.IndentFigure;
import net.officefloor.eclipse.common.figure.ListFigure;
import net.officefloor.eclipse.common.figure.ListItemFigure;
import net.officefloor.eclipse.common.figure.WrappingFigure;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link org.eclipse.draw2d.Figure} for the
 * {@link net.officefloor.model.desk.DeskWorkModel}.
 * 
 * @author Daniel
 */
public class DeskWorkFigure extends WrappingFigure {

	/**
	 * Initiate.
	 * 
	 * @param workName
	 *            Name of work.
	 */
	public DeskWorkFigure(String workName) {
		super(new ListFigure());

		// Specify layout
		ToolbarLayout layout = new ToolbarLayout();
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		this.setLayoutManager(layout);

		// Decorate this figure
		this.setBackgroundColor(ColorConstants.lightBlue);
		this.setOpaque(true);

		// Specify name of work
		this.addDecorate(new ListItemFigure(workName));

		// Indent the listing of tasks
		this.addDecorate(new IndentFigure(10, this.getChildContainerFigure()));
	}

}
