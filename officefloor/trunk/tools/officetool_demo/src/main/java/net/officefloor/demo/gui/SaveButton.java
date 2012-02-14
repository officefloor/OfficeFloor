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
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.store.MacroStore;

/**
 * {@link JButton} to save the current recording.
 * 
 * @author Daniel Sagenschneider
 */
public class SaveButton extends JButton {

	/**
	 * Initiate.
	 * 
	 * @param demo
	 *            {@link DemoTool}.
	 * @param frame
	 *            {@link Frame}.
	 */
	public SaveButton(final DemoTool demo, final Frame frame) {
		super("Save");

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Select the file for saving
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

				// Selected file to save (so obtain the file)
				File saveFile = chooser.getSelectedFile();

				// Ensure ends with recording file extension
				String extension = "." + DemoTool.RECORDING_FILE_EXTENSION;
				if (!saveFile.getName().endsWith(extension)) {
					saveFile = new File(saveFile.getParent(), saveFile
							.getName()
							+ extension);
				}

				// Determine if file exists
				if (saveFile.exists()) {
					switch (JOptionPane.showConfirmDialog(frame,
							"Overwrite file?")) {
					case JOptionPane.OK_OPTION:
						// Continue on to save macros
						break;
					default:
						// Do not save
						return;
					}
				}

				// Obtain the listing of all macros
				List<Macro> macros = new LinkedList<Macro>();
				MacroList macroList = demo.getMacroList();
				for (int i = 0; i < macroList.size(); i++) {
					macros.add(macroList.getItem(i).getMacro());
				}

				// Save recording to the file
				try {
					FileWriter writer = new FileWriter(saveFile);
					new MacroStore()
							.store(macros.toArray(new Macro[0]), writer);
					writer.close();
				} catch (IOException ex) {
					// Indicate failure storing to file
					JOptionPane.showMessageDialog(frame,
							"Failed to store recording to file: "
									+ ex.getMessage() + " ["
									+ ex.getClass().getSimpleName() + "]");
				}
			}
		});
	}

}