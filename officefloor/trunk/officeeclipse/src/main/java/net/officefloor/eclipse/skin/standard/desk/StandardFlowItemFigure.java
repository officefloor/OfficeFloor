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

import net.officefloor.eclipse.skin.desk.FlowItemFigure;
import net.officefloor.eclipse.skin.desk.FlowItemFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.CheckBox;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;

/**
 * {@link StandardFlowItemFigure} implementation.
 * 
 * @author Daniel
 */
public class StandardFlowItemFigure extends AbstractOfficeFloorFigure implements
		FlowItemFigure {

	/**
	 * Is public {@link CheckBox}.
	 */
	private final CheckBox isPublic;

	/**
	 * Initiate.
	 */
	public StandardFlowItemFigure(final FlowItemFigureContext context) {

		Figure figure = new Figure();
		figure.setLayoutManager(new ToolbarLayout(false));
		figure.setBackgroundColor(ColorConstants.lightGreen);
		figure.setOpaque(true);

		// Flow name
		Label flowItemName = new Label(context.getFlowItemName());
		flowItemName.setLayoutManager(new FlowLayout());

		// Is public
		this.isPublic = new CheckBox();
		this.isPublic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				context.setIsPublic(StandardFlowItemFigure.this.isPublic
						.isSelected());
			}
		});

		// Add the header
		Figure flowItemHeader = new Figure();
		flowItemHeader.setLayoutManager(new ToolbarLayout(true));
		flowItemHeader.add(flowItemName);
		flowItemHeader.add(this.isPublic);
		figure.add(flowItemHeader);

		// Add the content pane
		Figure content = new Figure();
		content.setLayoutManager(new ToolbarLayout(false));
		figure.add(content);

		// Specify figures
		this.setFigure(figure);
		this.setContentPane(content);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.eclipse.skin.desk.FlowItemFigure#setIsPublic(boolean)
	 */
	@Override
	public void setIsPublic(boolean isPublic) {
		this.isPublic.setSelected(isPublic);
	}

}
