/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macrolist.MacroList;

/**
 * {@link JButton} to delete selected {@link Macro} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class DeleteButton extends JButton {

	/**
	 * Initiate.
	 * 
	 * @param app
	 *            {@link DemoApp}.
	 */
	public DeleteButton(final DemoApp app) {
		super("Delete");

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {

				// Obtain the indices of macros to delete
				int[] indices = app.getSelectedMacroIndices();
				if ((indices == null) || (indices.length == 0)) {
					JOptionPane.showMessageDialog(app,
							"Please select macros for deletion");
					return; // nothing to delete
				}

				// Confirm that wish to delete macros
				StringBuilder text = new StringBuilder();
				text.append("Please confirm deletion of macros:");
				for (int index : indices) {
					text.append(" ");
					text.append(index);
				}
				switch (JOptionPane.showConfirmDialog(app, text.toString())) {
				case JOptionPane.OK_OPTION:
					// Delete the macros (in reverse order to maintain indices)
					MacroList macros = app.getMacroList();
					for (int i = (indices.length - 1); i >= 0; i--) {
						macros.removeItem(indices[i]);
					}
					break;
				}
			}
		});
	}

}