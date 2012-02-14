/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.play.MacroPlayer;
import net.officefloor.demo.record.RecordComponent;

/**
 * {@link JButton} to play the {@link Macro} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class PlayButton extends JButton {

	/**
	 * Initiate.
	 * 
	 * @param demo
	 *            {@link DemoTool}.
	 * @param frame
	 *            {@link Frame}.
	 * @param recordComponent
	 *            {@link RecordComponent}.
	 */
	public PlayButton(final DemoTool demo, final Frame frame,
			final RecordComponent recordComponent) {
		super("Play");

		// Run macros on clicking button
		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// Obtain the listing of selected macros
					Macro[] macros = demo.getSelectedMacros(true);

					// Obtain the reference point for playing macros
					Point offset = recordComponent.getLocationOnScreen();

					// Obtain hide/show macros
					Macro hideMacro = recordComponent.getHideFrameMacro();
					Macro showMacro = recordComponent.getShowFrameMacro();

					// Add the hide to beginning and show to end
					Macro[] runMacros = new Macro[macros.length + 2];
					runMacros[0] = hideMacro;
					for (int i = 0; i < macros.length; i++) {
						runMacros[i + 1] = macros[i];
					}
					runMacros[runMacros.length - 1] = showMacro;

					// Play the macros (hiding and showing frame)
					MacroPlayer player = new MacroPlayer(5, offset);
					player.play(runMacros);

				} catch (AWTException ex) {
					// Indicate failure to initiate playing macros
					JOptionPane.showMessageDialog(frame,
							"Failed to initiate player: " + ex.getMessage()
									+ " [" + ex.getClass().getSimpleName()
									+ "]", "Player error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}

}