/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.demo.gui;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.play.MacroPlayer;

/**
 * {@link JButton} to play the {@link Macro} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class PlayButton extends JButton {

	/**
	 * Initiate.
	 * 
	 * @param app
	 *            {@link DemoApp}.
	 */
	public PlayButton(final DemoApp app) {
		super("Play");

		// Run macros on clicking button
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Obtain the listing of selected macros
				Macro[] macros = app.getSelectedMacros(true);

				// Obtain the reference point for playing macros
				Point offset = app.getLocationOnScreen();

				// Hide the frame for playing
				app.setVisible(false);
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					// Ignore
				}

				// Play the macros
				try {
					MacroPlayer player = new MacroPlayer(5, offset);
					player.play(macros);
				} catch (AWTException ex) {
					JOptionPane.showMessageDialog(app,
							"Failed to initiate player: " + ex.getMessage()
									+ " [" + ex.getClass().getSimpleName()
									+ "]", "Player error",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					// Ensure frame is made visible again
					try {
						Thread.sleep(500);
					} catch (InterruptedException ex) {
						// Ignore
					}
					app.setVisible(true);
				}
			}
		});
	}

}