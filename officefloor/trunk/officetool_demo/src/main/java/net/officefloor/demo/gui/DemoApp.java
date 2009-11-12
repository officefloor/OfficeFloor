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
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import net.officefloor.demo.macro.InputTextMacro;
import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.RightClickMacro;
import net.officefloor.demo.macrolist.MacroItem;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.macrolist.MacroListListener;
import net.officefloor.demo.record.RecordComponent;

public class DemoApp extends JFrame {

	/**
	 * Provides ability to run the Demo Tool from command line.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String[] args) throws AWTException {

		// Create the demo application frame
		final DemoApp frame = new DemoApp();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		// Run the application
		frame.setSize(800, 600);
		frame.setVisible(true);
	}

	/**
	 * Extension for recording file.
	 */
	public static final String RECORDING_FILE_EXTENSION = "rcd";

	/**
	 * {@link MacroList} containing the {@link Macro} items.
	 */
	private final MacroList macros;

	/**
	 * {@link JList} for the {@link Macro} instances.
	 */
	private final JList macroList;

	/**
	 * Initiate.
	 * 
	 * @throws AWTException
	 *             If running in headless environment.
	 */
	public DemoApp() throws AWTException {

		// Create the panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		this.add(panel);

		// Create the macro listing (ensuring that they keep in sync)
		final DefaultListModel macroListModel = new DefaultListModel();
		this.macros = new MacroList(new MacroListListener() {
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
		RecordComponent recorder = new RecordComponent(robot, this, this.macros);
		recorder.setMinimumSize(new Dimension(640, 420));
		recorder.setPreferredSize(new Dimension(640, 420));
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
		this.macroList = new JList(macroListModel);
		controlPanel.add(this.macroList);

		// Provide buttons
		controlPanel.add(new PlayButton(this));
		controlPanel.add(new SaveButton(this));
		controlPanel.add(new OpenButton(this));
		controlPanel.add(new DeleteButton(this));
	}

	/**
	 * Obtains the selected {@link Macro} instances.
	 * 
	 * @param isReturnAllIfNoneSelected
	 *            <code>true</code> indicates to return all {@link Macro}
	 *            instances if none are selected.
	 * @return Selected {@link Macro} instances.
	 */
	public Macro[] getSelectedMacros(boolean isReturnAllIfNoneSelected) {

		// Obtain the listing of selected macros
		List<Macro> selectedMacros = new LinkedList<Macro>();
		for (int index : this.macroList.getSelectedIndices()) {
			Macro macro = this.macros.getItem(index).getMacro();
			selectedMacros.add(macro);
		}
		if (isReturnAllIfNoneSelected && (selectedMacros.size() == 0)) {
			// Nothing selected, so play all macros
			for (int i = 0; i < this.macros.size(); i++) {
				Macro macro = this.macros.getItem(i).getMacro();
				selectedMacros.add(macro);
			}
		}

		// Return the macros
		return selectedMacros.toArray(new Macro[0]);
	}

	/**
	 * Obtains the indices of the selected {@link Macro} instances.
	 * 
	 * @return Indices of the selected {@link Macro} instances.
	 */
	public int[] getSelectedMacroIndices() {
		return this.macroList.getSelectedIndices();
	}

	/**
	 * Obtains the {@link MacroList}.
	 * 
	 * @return {@link MacroList}.
	 */
	public MacroList getMacroList() {
		return this.macros;
	}
}