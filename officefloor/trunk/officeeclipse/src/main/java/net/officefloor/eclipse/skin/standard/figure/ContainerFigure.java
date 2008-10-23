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
package net.officefloor.eclipse.skin.standard.figure;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * {@link RoundedRectangle} that has a header section and content pane.
 * 
 * @author Daniel
 */
public class ContainerFigure extends Figure {

	/**
	 * Is public {@link Figure}.
	 */
	private final Figure isPublicFigure;

	/**
	 * Content pane {@link Figure}.
	 */
	private final Figure contentPane;

	/**
	 * Initiate.
	 * 
	 * @param containerName
	 *            Name of the container.
	 * @param backgroundColour
	 *            Background {@link Color} of the container.
	 * @param contentPaneLeftInset
	 *            Left inset of items added to this container.
	 * @param includeIsPublicFigure
	 *            <code>true</code> if including public.
	 */
	public ContainerFigure(String containerName, Color backgroundColour,
			int contentPaneLeftInset, boolean includeIsPublicFigure) {

		// Specify the layout
		this.setLayoutManager(new NoSpacingToolbarLayout(false));

		// Create the rounded rectangle container
		RoundedRectangle container = new RoundedRectangle();
		NoSpacingGridLayout containerLayout = new NoSpacingGridLayout(1);
		container.setLayoutManager(containerLayout);
		container.setBackgroundColor(backgroundColour);
		container.setOpaque(true);
		this.add(container);

		// Create the header
		Figure header = new Figure();
		GridLayout headerLayout = new GridLayout(
				(includeIsPublicFigure ? 2 : 1), false);
		header.setLayoutManager(headerLayout);
		container.add(header);

		// Include is public figure if necessary
		if (!includeIsPublicFigure) {
			this.isPublicFigure = null;
		} else {
			this.isPublicFigure = new Ellipse();
			this.isPublicFigure.setSize(6, 6);
			this.isPublicFigure.setBackgroundColor(ColorConstants.black);
			header.add(this.isPublicFigure);
		}

		// Specify the container name
		Label containerLabel = new Label(containerName);
		containerLabel.setLayoutManager(new NoSpacingToolbarLayout(true));
		header.add(containerLabel);

		// Content pane
		Figure contentPaneWrap = new Figure();
		contentPaneWrap.setLayoutManager(new NoSpacingToolbarLayout(false));
		contentPaneWrap.setBorder(new ContentBorder());
		this.contentPane = new Figure();
		NoSpacingToolbarLayout contentLayout = new NoSpacingToolbarLayout(false);
		this.contentPane.setLayoutManager(contentLayout);
		this.contentPane.setBorder(new MarginBorder(2, contentPaneLeftInset, 2,
				2));
		contentPaneWrap.add(this.contentPane);
		container.add(contentPaneWrap);
		containerLayout.setConstraint(contentPaneWrap, new GridData(SWT.FILL,
				0, true, false));
	}

	/**
	 * Obtains the content pane.
	 * 
	 * @return Content pane.
	 */
	public Figure getContentPane() {
		return this.contentPane;
	}

	/**
	 * Specifies if public.
	 * 
	 * @param isPublic
	 *            <code>true</code> if public.
	 */
	public void setIsPublic(boolean isPublic) {
		this.isPublicFigure.setVisible(isPublic);
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
