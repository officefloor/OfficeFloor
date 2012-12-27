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
 * {@link MacroSource} for pausing.
 * 
 * @author Daniel Sagenschneider
 */
public class PauseMacro implements MacroSource, Macro, MacroTask {

	/**
	 * Pause time.
	 */
	private long pauseTime;

	/**
	 * Obtains the pause time.
	 * 
	 * @return Pause time.
	 */
	public long getPauseTime() {
		return this.pauseTime;
	}

	/*
	 * =================== MacroSource =========================
	 */

	@Override
	public String getDisplayName() {
		return "Pause";
	}

	@Override
	public void sourceMacro(MacroSourceContext context) {

		// Obtain pause time
		String inputTime = JOptionPane.showInputDialog("Pause time (ms):");

		// Create and return the macro
		PauseMacro macro = new PauseMacro();
		macro.pauseTime = Long.parseLong(inputTime);
		context.setNewMacro(macro);
	}

	/*
	 * =================== Macro =========================
	 */

	@Override
	public void setConfigurationMemento(String memento) {
		this.pauseTime = Long.parseLong(memento);
	}

	@Override
	public String getConfigurationMemento() {
		return String.valueOf(this.pauseTime);
	}

	@Override
	public String getDisplayLabel() {
		return "Pause " + this.pauseTime + "ms";
	}

	@Override
	public Point getStartingMouseLocation() {
		return null;
	}

	@Override
	public MacroTask[] getMacroTasks() {
		return new MacroTask[] { this };
	}

	/*
	 * ====================== MacroTask ==========================
	 */

	@Override
	public void runMacroTask(MacroTaskContext context) {
		// Do nothing as only wanting to pause
	}

	@Override
	public long getPostRunWaitTime() {
		return this.pauseTime;
	}

}