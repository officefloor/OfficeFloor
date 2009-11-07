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
import java.awt.FlowLayout;
import java.awt.Robot;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macrolist.MacroItem;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.macrolist.MacroListListener;
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
		JFrame frame = new JFrame();
		frame.setLayout(new FlowLayout());

		// Create the panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		frame.add(panel);

		// Create the macro listing
		final DefaultListModel macroListModel = new DefaultListModel();
		MacroList macroList = new MacroList(new MacroListListener() {
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
		recorder.setPreferredSize(new Dimension(640, 420));
		panel.add(recorder);

		// Add the macro factories
		recorder.addMacro(new LeftClickMacro());

		// Add panel with listing of added macros
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
		panel.add(controlPanel);

		// Provide listing of the macros
		controlPanel.add(new JLabel("Macros"));
		controlPanel.add(new JList(macroListModel));

		// Run the application
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

}