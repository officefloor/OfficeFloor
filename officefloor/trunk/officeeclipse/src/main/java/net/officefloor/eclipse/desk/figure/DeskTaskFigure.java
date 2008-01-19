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
import net.officefloor.eclipse.common.figure.WrappingFigure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link Figure} for the {@link net.officefloor.model.desk.DeskTaskModel}.
 * 
 * @author Daniel
 */
public class DeskTaskFigure extends WrappingFigure {

	/**
	 * Initiate.
	 * 
	 * @param taskName
	 *            Name of task.
	 * @param addAsFlowItem
	 *            Figure to add flow item for this task.
	 */
	public DeskTaskFigure(String taskName, IFigure addAsFlowItem) {
		super(new ListFigure());

		// Add name of task and ability to add as flow item
		Figure taskHeader = new Figure();
		taskHeader.setLayoutManager(new ToolbarLayout(true));
		taskHeader.add(new ListItemFigure(taskName));
		taskHeader.add(addAsFlowItem);
		this.addDecorate(taskHeader);

		// Indent all objects of tasks
		this.addDecorate(new IndentFigure(10, this.getChildContainerFigure()));
	}

}
