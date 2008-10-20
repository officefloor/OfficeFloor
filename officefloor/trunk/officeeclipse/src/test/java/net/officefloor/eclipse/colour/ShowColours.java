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
package net.officefloor.eclipse.colour;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * Shows the colours.
 * 
 * @author Daniel
 */
public class ShowColours {

	/**
	 * Starting point.
	 * 
	 * @param arguments
	 *            Command line arguments.
	 */
	public static void main(String... arguments) {

		final int SIZE = 256;

		// Create the contents
		Figure contents = new Figure();
		XYLayout layout = new XYLayout();
		contents.setLayoutManager(layout);

		// Create the display
		final Display display = new Display();
		final Shell shell = new Shell(display);
		shell.setText("Colours");
		shell.setSize(300, 350);
		LightweightSystem lws = new LightweightSystem(shell);

		// Fill the contents with colours
		int z = 0;
		Figure[][] colourPoints = new Figure[SIZE][SIZE];
		for (int x = 0; x < SIZE; x++) {
			for (int y = 0; y < SIZE; y++) {
				Figure figure = new Figure();
				figure.setLayoutManager(new ToolbarLayout());
				figure.setBackgroundColor(new Color(null, x, y, z));
				figure.setOpaque(true);
				contents.add(figure);
				colourPoints[x][y] = figure;
				layout.setConstraint(figure,
						new Rectangle(x + 10, y + 10, 1, 1));
			}
		}

		// Startup display
		lws.setContents(contents);
		shell.open();
		while (!shell.isDisposed()) {
			while (!display.readAndDispatch()) {
				display.sleep();

				// Change figure colours
				z += 10;
				z %= SIZE;
				System.out.println("Colour: " + z);
				for (int x = 0; x < SIZE; x++) {
					for (int y = 0; y < SIZE; y++) {
						Figure figure = colourPoints[x][y];
						figure.setBackgroundColor(new Color(null, x, y, z));
						figure.setToolTip(new Label(x + "," + y + "," + z));
					}
				}
			}
		}

	}
}
