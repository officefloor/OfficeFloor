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
package net.officefloor.demo.macro;

import java.awt.Point;

import javax.swing.JOptionPane;

/**
 * {@link Macro} to input text.
 * 
 * @author Daniel Sagenschneider
 */
public class InputTextMacro implements MacroFactory, Macro {

	/**
	 * Text to input.
	 */
	private String text;

	/*
	 * ==================== MacroFactory =======================
	 */

	@Override
	public String getDisplayName() {
		return "Input text";
	}

	@Override
	public Macro createMacro(Point location) {

		// Obtain text to input
		String inputText = JOptionPane.showInputDialog("Input text:");

		// Create and return the macro
		InputTextMacro macro = new InputTextMacro();
		macro.text = inputText;
		return macro;
	}

	/*
	 * ======================= Macro ===========================
	 */

	@Override
	public String getConfigurationMemento() {
		return this.text;
	}

	@Override
	public void setConfigurationMemento(String memento) {
		this.text = memento;
	}

	@Override
	public Point getStartingMouseLocation() {
		// No starting location required
		return null;
	}

	@Override
	public void runMacro(MacroContext context) {
		// Input the text
		context.keyText(this.text);
	}

}