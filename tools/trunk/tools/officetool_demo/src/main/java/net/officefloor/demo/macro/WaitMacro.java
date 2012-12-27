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

/**
 * <p>
 * Allows {@link Thread} to wait (block) until this {@link Macro} is complete.
 * <p>
 * This is typically used as last {@link Macro} to run for testing to coordinate
 * running and validating output.
 * 
 * @author Daniel Sagenschneider
 */
public class WaitMacro implements Macro, MacroTask {

	/**
	 * Indicates if this {@link Macro} has been run.
	 */
	private boolean isRun = false;

	/**
	 * Blocks the invoking {@link Thread} until this {@link Macro} is run.
	 */
	public synchronized void waitUntilRun() {
		try {
			// Block until run
			for (;;) {

				// Determine if run
				if (this.isRun) {
					return; // run
				}

				// Wait until run
				this.wait(100);
			}
		} catch (InterruptedException ex) {
			// Carry on
		}
	}

	/*
	 * ====================== Macro ============================
	 */

	@Override
	public String getConfigurationMemento() {
		throw new IllegalStateException("Should not be stored");
	}

	@Override
	public void setConfigurationMemento(String memento) {
		throw new IllegalStateException("Should not be retrieved");
	}

	@Override
	public String getDisplayLabel() {
		return null;
	}

	@Override
	public Point getStartingMouseLocation() {
		// No mouse interaction as only to invoking block thread
		return null;
	}

	@Override
	public MacroTask[] getMacroTasks() {
		return new MacroTask[] { this };
	}

	/*
	 * ====================== Macro ============================
	 */

	@Override
	public synchronized void runMacroTask(MacroTaskContext context) {
		// Flag run
		this.isRun = true;
		this.notify();
	}

	@Override
	public long getPostRunWaitTime() {
		return 0;
	}

}