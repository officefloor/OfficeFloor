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
package net.officefloor.eclipse.common.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * Figure that shows another figure indented.
 * 
 * @author Daniel
 */
public class IndentFigure extends WrappingFigure {

	/**
	 * Initiate.
	 * 
	 * @param indent
	 *            Number of pixels to indent the figure.
	 * @param figure
	 *            Figure.
	 */
	public IndentFigure(int indent, Figure figure) {
		super(figure);
		
		// Configure layout
		ToolbarLayout layout = new ToolbarLayout(true);
		layout.setMinorAlignment(ToolbarLayout.ALIGN_TOPLEFT);
		this.setLayoutManager(layout);

		// Configure the indent figure
		Figure indentFigure = new Figure();
		indentFigure.setOpaque(false);
		indentFigure.setSize(indent, figure.getSize().height);
		this.addDecorate(indentFigure);

		// Add indented figure to contain children
		this.addChildContainerFigure();
	}

}
