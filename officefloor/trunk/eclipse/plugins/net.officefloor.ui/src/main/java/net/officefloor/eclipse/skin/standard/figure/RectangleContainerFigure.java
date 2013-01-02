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
package net.officefloor.eclipse.skin.standard.figure;

import net.officefloor.eclipse.skin.standard.StandardOfficeFloorColours;

import org.eclipse.draw2d.AbstractBorder;
import org.eclipse.draw2d.Border;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * {@link RectangleFigure} that has a header section and content pane.
 *
 * @author Daniel Sagenschneider
 */
public class RectangleContainerFigure extends Figure {

	/**
	 * Is public {@link Figure}.
	 */
	private final Figure isPublicFigure;

	/**
	 * Content pane {@link Figure}.
	 */
	private final Figure contentPane;

	/**
	 * {@link Label} for the container name.
	 */
	private final Label containerName;

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
	public RectangleContainerFigure(String containerName,
			Color backgroundColour, int contentPaneLeftInset,
			boolean includeIsPublicFigure) {

		// Specify the layout
		this.setLayoutManager(new NoSpacingToolbarLayout(false));

		// Create the rounded rectangle container
		RectangleFigure container = new RectangleFigure();
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
			this.isPublicFigure.setBackgroundColor(StandardOfficeFloorColours.BLACK());
			header.add(this.isPublicFigure);
		}

		// Specify the container name
		this.containerName = new Label(containerName);
		this.containerName.setLayoutManager(new NoSpacingToolbarLayout(true));
		header.add(this.containerName);

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
	 * Obtains the {@link Label} for the container name.
	 *
	 * @return {@link Label} for the container name.
	 */
	public Label getContainerName() {
		return this.containerName;
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
		 * ============ Border ================================
		 */
		@Override
		public Insets getInsets(IFigure figure) {
			return new Insets(0);
		}

		@Override
		public void paint(IFigure figure, Graphics graphics, Insets insets) {
			Rectangle paintRectangle = getPaintRectangle(figure, insets);
			graphics.drawLine(paintRectangle.getTopLeft(), paintRectangle
					.getTopRight());
		}
	}

}