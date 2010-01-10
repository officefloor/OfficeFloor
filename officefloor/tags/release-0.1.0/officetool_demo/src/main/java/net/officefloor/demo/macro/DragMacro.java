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
package net.officefloor.demo.macro;

import java.awt.Point;

/**
 * {@link Macro} to drag content.
 * 
 * @author Daniel Sagenschneider
 */
public class DragMacro implements MacroFactory, Macro {

	/*
	 * ====================== MacroFactory ==============================
	 */

	@Override
	public String getDisplayName() {
		return "Drag";
	}

	@Override
	public Macro createMacro(MacroFactoryContext context) {
		// TODO implement MacroFactory.createMacro
		throw new UnsupportedOperationException(
				"TODO implement MacroFactory.createMacro");
	}

	/*
	 * ========================= Macro =================================
	 */

	@Override
	public String getConfigurationMemento() {
		// TODO implement Macro.getConfigurationMemento
		throw new UnsupportedOperationException(
				"TODO implement Macro.getConfigurationMemento");
	}

	@Override
	public void setConfigurationMemento(String memento) {
		// TODO implement Macro.setConfigurationMemento
		throw new UnsupportedOperationException(
				"TODO implement Macro.setConfigurationMemento");
	}

	@Override
	public Point getStartingMouseLocation() {
		// TODO implement Macro.getStartingMouseLocation
		throw new UnsupportedOperationException(
				"TODO implement Macro.getStartingMouseLocation");
	}

	@Override
	public MacroTask[] getMacroTasks() {
		// TODO implement Macro.getMacroTasks
		throw new UnsupportedOperationException(
				"TODO implement Macro.getMacroTasks");
	}

}