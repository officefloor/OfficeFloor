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

import net.officefloor.eclipse.WoofPlugin;
import net.officefloor.eclipse.skin.standard.AbstractOfficeFloorFigure;
import net.officefloor.eclipse.skin.standard.figure.NoSpacingGridLayout;
import net.officefloor.eclipse.skin.woof.AccessFigure;
import net.officefloor.eclipse.skin.woof.AccessFigureContext;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.ImageFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Standard {@link AccessFigure}.
 * 
 * @author Daniel Sagenschneider
 */
public class StandardAccessFigure extends AbstractOfficeFloorFigure implements
		AccessFigure {

	/**
	 * Initiate.
	 * 
	 * @param context
	 *            {@link AccessFigureContext}.
	 */
	public StandardAccessFigure(AccessFigureContext context) {

		final Color titleBarTopColour = new Color(null, 255, 248, 220);
		final Color titleBarBottomColour = new Color(null, 131, 76, 36);

		// Create the rounded rectangle container
		RoundedRectangle container = new RoundedRectangle();
		NoSpacingGridLayout containerLayout = new NoSpacingGridLayout(1);
		container.setLayoutManager(containerLayout);
		container.setBackgroundColor(titleBarTopColour);
		container.setOutline(false);

		// Create the access image
		final String ACCESS_IMAGE_KEY = WoofPlugin.PLUGIN_ID + ".access";
		Image accessImage = JFaceResources.getImageRegistry().get(
				ACCESS_IMAGE_KEY);
		if (accessImage == null) {
			// Create and register the image
			ImageDescriptor secureImageData = WoofPlugin
					.getImageDescriptor("icons/key.png");
			accessImage = secureImageData.createImage();
			JFaceResources.getImageRegistry()
					.put(ACCESS_IMAGE_KEY, accessImage);
		}

		// Add the title bar
		TitleBarFigure titleBar = new TitleBarFigure(titleBarTopColour,
				titleBarBottomColour, new ImageFigure(accessImage));
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

}