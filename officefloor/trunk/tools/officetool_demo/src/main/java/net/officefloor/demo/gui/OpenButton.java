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

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.store.MacroStore;

/**
 * {@link JButton} to open a stored recording.
 * 
 * @author Daniel Sagenschneider
 */
public class OpenButton extends JButton {

	/**
	 * Initiate.
	 * 
	 * @param demo
	 *            {@link DemoTool}.
	 */
	public OpenButton(final DemoTool demo, final Frame frame) {
		super("Open");

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Select the file for opening
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(new FileNameExtensionFilter(
						"Recording file", DemoTool.RECORDING_FILE_EXTENSION));
				chooser.setAcceptAllFileFilterUsed(false);
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				switch (chooser.showSaveDialog(frame)) {
				case JFileChooser.APPROVE_OPTION:
					// Continue on to save macros
					break;
				default:
					// Do not save
					return;
				}

				// Selected file to open (so obtain the file)
				File openFile = chooser.getSelectedFile();

				// Read in the macros
				Macro[] macros;
				try {
					FileReader reader = new FileReader(openFile);
					macros = new MacroStore().retrieve(reader);
					reader.close();
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(frame,
							"Failed to load macros: " + ex.getMessage() + " ["
									+ ex.getClass().getSimpleName() + "]");
					return; // failed to load
				}

				// Load the macros
				MacroList macroList = demo.getMacroList();
				for (Macro macro : macros) {
					macroList.addMacro(macro);
				}
			}
		});
	}

}