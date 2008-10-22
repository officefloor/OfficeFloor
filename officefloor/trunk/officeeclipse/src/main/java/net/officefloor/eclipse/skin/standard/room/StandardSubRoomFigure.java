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

import net.officefloor.eclipse.skin.room.SubRoomFigure;
import net.officefloor.eclipse.skin.room.SubRoomFigureContext;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link SubRoomFigure}.
 * 
 * @author Daniel
 */
public class StandardSubRoomFigure extends AbstractOfficeFloorFigure implements
		SubRoomFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link SubRoomFigureContext}.
	 */
	public StandardSubRoomFigure(SubRoomFigureContext context) {

		Color subRoomColour = new Color(null, 130, 255, 150);
		
		// Create the sub room container
		RoundedRectangle subRoomFigure = new RoundedRectangle();
		NoSpacingGridLayout subRoomLayout = new NoSpacingGridLayout(1);
		subRoomFigure.setLayoutManager(subRoomLayout);
		subRoomFigure.setBackgroundColor(subRoomColour);
		subRoomFigure.setOpaque(true);

		// Create the header
		Figure header = new Figure();
		GridLayout headerLayout = new GridLayout(1, false);
		header.setLayoutManager(headerLayout);
		header.setBorder(new MarginBorder(0, 5, 0, 0));
		subRoomFigure.add(header);

		// Specify the flow item name
		Label flowItemName = new Label(context.getSubRoomName());
		flowItemName.setLayoutManager(new NoSpacingToolbarLayout(true));
		header.add(flowItemName);

		// Content pane
		Figure contentPaneWrap = new Figure();
		contentPaneWrap.setLayoutManager(new ToolbarLayout());
		contentPaneWrap.setBorder(new ContentBorder());
		Figure contentPane = new Figure();
		ToolbarLayout contentLayout = new ToolbarLayout(false);
		contentPane.setLayoutManager(contentLayout);
		contentPane.setBorder(new MarginBorder(2, 5, 2, 2));
		contentPaneWrap.add(contentPane);
		subRoomFigure.add(contentPaneWrap);
		subRoomLayout.setConstraint(contentPaneWrap, new GridData(SWT.FILL, 0,
				true, false));

		// Specify the figures
		this.setFigure(subRoomFigure);
		this.setContentPane(contentPane);
	}
	
	/**
	 * {@link Border} for the content.
	 */
	private class ContentBorder extends AbstractBorder {

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.Border#getInsets(org.eclipse.draw2d.IFigure)
		 */
		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.draw2d.Border#paint(org.eclipse.draw2d.IFigure,
		 * org.eclipse.draw2d.Graphics, org.eclipse.draw2d.geometry.Insets)
		 */
		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			Rectangle paintRectangle = getPaintRectangle(figure, insets);
			graphics.drawLine(paintRectangle.getTopLeft(), paintRectangle
					.getTopRight());
		}
	}

}
