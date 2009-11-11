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
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.officefloor.demo.macro.InputTextMacro;
import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.RightClickMacro;
import net.officefloor.demo.macrolist.MacroItem;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.macrolist.MacroListListener;
import net.officefloor.demo.play.MacroPlayer;
import net.officefloor.demo.record.RecordComponent;

public class DemoApp {

	/**
	 * Starts the demo application.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) throws AWTException {

		// Create the application

		// Create the frame for the recording
		final JFrame frame = new JFrame();

		// Create the panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		frame.add(panel);

		// Create the macro listing
		final DefaultListModel macroListModel = new DefaultListModel();
		final MacroList macroList = new MacroList(new MacroListListener() {
			@Override
			public void macroAdded(MacroItem item, int index) {
				macroListModel.add(index, item.getMacro().getClass()
						.getSimpleName());
			}

			@Override
			public void macroRemoved(MacroItem item, int index) {
				macroListModel.remove(index);
			}
		});

		// Create the robot
		Robot robot = new Robot();

		// Create the record component
		RecordComponent recorder = new RecordComponent(robot, frame, macroList);
		recorder.setMinimumSize(new Dimension(640, 420));
		recorder.setPreferredSize(new Dimension(800, 600));
		panel.add(recorder);

		// Add the macro factories
		recorder.addMacro(new LeftClickMacro());
		recorder.addMacro(new RightClickMacro());
		recorder.addMacro(new InputTextMacro());

		// Add panel with listing of added macros
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		panel.add(controlPanel);

		// Provide listing of the macros
		controlPanel.add(new JLabel("Macros"));
		final JList macroJList = new JList(macroListModel);
		controlPanel.add(macroJList);

		// Provide play button
		JButton playButton = new JButton("Play");
		playButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Obtain the listing of selected macros
				List<Macro> macros = new LinkedList<Macro>();
				for (int index : macroJList.getSelectedIndices()) {
					Macro macro = macroList.getItem(index).getMacro();
					macros.add(macro);
				}
				if (macros.size() == 0) {
					// Nothing selected, so play all macros
					for (int i = 0; i < macroList.size(); i++) {
						Macro macro = macroList.getItem(i).getMacro();
						macros.add(macro);
					}
				}

				// Hide the frame for playing
				frame.setVisible(false);
				try {
					Thread.sleep(500);
				} catch (InterruptedException ex) {
					// Ignore
				}

				// Play the macros
				try {
					MacroPlayer player = new MacroPlayer(5);
					player.play(macros.toArray(new Macro[0]));
				} catch (AWTException ex) {
					JOptionPane.showMessageDialog(frame,
							"Failed to initiate player: " + ex.getMessage()
									+ " [" + ex.getClass().getSimpleName()
									+ "]", "Player error",
							JOptionPane.ERROR_MESSAGE);
				} finally {
					// Ensure frame is made visible again
					frame.setVisible(true);
				}
			}
		});
		controlPanel.add(playButton);

		// Run the application
		frame.setSize(1000, 800);
		frame.setVisible(true);
	}

}