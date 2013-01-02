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
package net.officefloor.eclipse.skin.standard.woof;

import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingToolbarLayout;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Pattern;

/**
 * Title bar {@link IFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class TitleBarFigure extends RoundedRectangle {

	/**
	 * Top {@link Color} of title bar.
	 */
	private final Color topColour;

	/**
	 * Bottom {@link Color} of title bar.
	 */
	private final Color bottomColour;

	/**
	 * Title name {@link Label}.
	 */
	private final Label titleNameFigure;

	/**
	 * Initiate.
	 * 
	 * @param titleName
	 *            Title name.
	 * @param textColour
	 *            Text {@link Color}.
	 * @param topColour
	 *            Top {@link Color} of title bar.
	 * @param bottomColour
	 *            Bottom {@link Color} of title bar.
	 */
	public TitleBarFigure(String titleName, Color textColour, Color topColour,
			Color bottomColour) {
		this.topColour = topColour;
		this.bottomColour = bottomColour;

		// Configure this figure
		this.setLayoutManager(new NoSpacingGridLayout(1));
		this.setOutline(false);

		// Specify the title name
		this.titleNameFigure = new Label(titleName);
		this.titleNameFigure.setLayoutManager(new NoSpacingToolbarLayout(true));
		this.titleNameFigure.setForegroundColor(textColour);
		this.titleNameFigure.setBorder(new MarginBorder(2, 2, 2, 2));
		this.add(this.titleNameFigure);
	}

	/**
	 * Obtains the title name {@link IFigure}.
	 * 
	 * @return Title name {@link IFigure}.
	 */
	public Label getTitleNameFigure() {
		return this.titleNameFigure;
	}

	/*
	 * =================== Figure ================================
	 */

	@Override
	public void paintClientArea(Graphics graphics) {

		// Provide gradient background
		graphics.pushState();
		Rectangle rect = new Rectangle(this.bounds);
		graphics.setBackgroundPattern(new Pattern(null, rect.x, rect.y, rect.x,
				rect.y + rect.height, this.topColour, this.bottomColour));
		Dimension corner = this.getCornerDimensions();
		graphics.fillRoundRectangle(rect, corner.width, corner.height);
		graphics.popState();

		// Flatten bottom (with gradient)
		graphics.pushState();
		int flattenHeight = corner.height;
		Color flattenTopColour = new Color(null, gradientStartRgbValue(
				rect.height, flattenHeight, this.topColour.getRed(),
				this.bottomColour.getRed()), gradientStartRgbValue(rect.height,
				flattenHeight, this.topColour.getGreen(),
				this.bottomColour.getGreen()), gradientStartRgbValue(
				rect.height, flattenHeight, this.topColour.getBlue(),
				this.bottomColour.getBlue()));
		Rectangle flatten = new Rectangle(rect.x,
				(rect.y + rect.height - flattenHeight), rect.width,
				flattenHeight);
		graphics.setBackgroundPattern(new Pattern(null, flatten.x, flatten.y,
				flatten.x, flatten.y + flatten.height, flattenTopColour,
				this.bottomColour));
		graphics.fillRectangle(flatten);
		graphics.popState();

		// Paint in children
		super.paintClientArea(graphics);
	}

	/**
	 * Calculate the gradient start.
	 * 
	 * @param totalHeight
	 *            Total height of figure.
	 * @param flattenHeight
	 *            Flatten height of figure.
	 * @param topRgbValue
	 *            Top RGB value.
	 * @param bottomRgbValue
	 *            Bottom RBB value.
	 * @return Gradient RGB value.
	 */
	private static int gradientStartRgbValue(int totalHeight,
			int flattenHeight, int topRgbValue, int bottomRgbValue) {

		// Determine weighting for start
		float weighting = (totalHeight - flattenHeight) / ((float) totalHeight);

		// Determine the difference
		int difference = bottomRgbValue - topRgbValue;

		// Calculate increment on colour from top
		float increment = difference * weighting;

		// Provide start RGB value
		return (int) (topRgbValue + increment);
	}

}