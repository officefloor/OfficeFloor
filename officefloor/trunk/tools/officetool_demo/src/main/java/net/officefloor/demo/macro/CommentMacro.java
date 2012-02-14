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

package net.officefloor.demo.macro;

import java.awt.Point;

import javax.swing.JOptionPane;

/**
 * Comment {@link MacroSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class CommentMacro implements MacroSource, Macro {

	/**
	 * Comment.
	 */
	private String comment;

	/**
	 * Obtains the comment.
	 * 
	 * @return Comment.
	 */
	public String getComment() {
		return this.comment;
	}

	/*
	 * ==================== MacroSource =======================
	 */

	@Override
	public String getDisplayName() {
		return "Comment";
	}

	@Override
	public void sourceMacro(MacroSourceContext context) {

		// Obtain text to input
		String inputText = JOptionPane.showInputDialog("Comment:");

		// Create and return the macro
		CommentMacro macro = new CommentMacro();
		macro.comment = inputText;
		context.setNewMacro(macro);
	}

	/*
	 * ======================= Macro ===========================
	 */

	@Override
	public String getConfigurationMemento() {
		return this.comment;
	}

	@Override
	public void setConfigurationMemento(String memento) {
		this.comment = memento;
	}

	@Override
	public String getDisplayLabel() {

		final int MAX_TEXT_LENGTH = 20;

		// Trim comment
		String label = this.comment;
		label = (label.length() > MAX_TEXT_LENGTH ? label.substring(0,
				MAX_TEXT_LENGTH) : label);

		// Return the display label
		return "[" + label + "]";
	}

	@Override
	public Point getStartingMouseLocation() {
		// No starting location required
		return null;
	}

	@Override
	public MacroTask[] getMacroTasks() {
		// No tasks as comment
		return new MacroTask[0];
	}

}