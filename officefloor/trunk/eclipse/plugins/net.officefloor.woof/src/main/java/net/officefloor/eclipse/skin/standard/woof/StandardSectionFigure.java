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

import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.woof.SectionFigure;
import net.officefloor.eclipse.skin.woof.SectionFigureContext;
import net.officefloor.eclipse.skin.woof.TemplateFigureContext;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;

/**
 * Standard {@link SectionFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardSectionFigure extends AbstractOfficeFloorFigure implements
		SectionFigure {

	/**
	 * Name.
	 */
	private final Label name;

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link TemplateFigureContext}.
	 */
	public StandardSectionFigure(SectionFigureContext context) {

		final Color titleBarTextColour = new Color(null, 255, 255, 255);
		final Color titleBarTopColour = new Color(null, 177, 232, 177);
		final Color titleBarBottomColour = new Color(null, 0, 127, 0);

		// Create the rounded rectangle container
		RoundedRectangle container = new RoundedRectangle();
		NoSpacingGridLayout containerLayout = new NoSpacingGridLayout(1);
		container.setLayoutManager(containerLayout);
		container.setBackgroundColor(titleBarTopColour);
		container.setOutline(false);

		// Add the title bar
		TitleBarFigure titleBar = new TitleBarFigure(context.getSectionName(),
				titleBarTextColour, titleBarTopColour, titleBarBottomColour);
		this.name = titleBar.getTitleNameFigure();
		container.add(titleBar);
		containerLayout.setConstraint(titleBar, new GridData(SWT.FILL, 0, true,
				false));

		// Provide window border to content
		Figure contentPane = new Figure();
		contentPane.setLayoutManager(new NoSpacingGridLayout(1));
		contentPane.setBorder(new MarginBorder(2, 2, 2, 2));
		container.add(contentPane);
		containerLayout.setConstraint(contentPane, new GridData(SWT.FILL, 0,
				true, false));

		// Specify the figures
		this.setFigure(container);
		this.setContentPane(contentPane);
	}

	/*
	 * ===================== SectionFigure ==========================
	 */

	@Override
	public void setSectionName(String sectionName) {
		this.name.setText(sectionName);
	}

}