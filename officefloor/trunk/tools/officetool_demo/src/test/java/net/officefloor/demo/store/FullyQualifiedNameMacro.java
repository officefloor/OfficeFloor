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

package net.officefloor.demo.store;

import java.awt.Point;

import junit.framework.TestCase;
import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroTask;

/**
 * Test {@link Macro} not contained in the {@link Macro} package so requires
 * fully qualified name.
 * 
 * @author Daniel Sagenschneider
 */
public class FullyQualifiedNameMacro implements Macro {

	/**
	 * Configuration.
	 */
	private String configuration;

	/*
	 * ====================== Macro ===========================
	 */

	@Override
	public String getConfigurationMemento() {
		return this.configuration;
	}

	@Override
	public void setConfigurationMemento(String memento) {
		this.configuration = memento;
	}

	@Override
	public String getDisplayLabel() {
		return null;
	}

	@Override
	public Point getStartingMouseLocation() {
		return null;
	}

	@Override
	public MacroTask[] getMacroTasks() {
		TestCase.fail("Macro should not be run");
		return null;
	}

}