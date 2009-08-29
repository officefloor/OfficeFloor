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
import java.awt.event.InputEvent;

/**
 * {@link Macro} to left click.
 *
 * @author Daniel Sagenschneider
 */
public class LeftClickMacro implements Macro, MacroFactory {

	/**
	 * Location.
	 */
	private Point location;

	/*
	 * ================ MacroFactory =================================
	 */

	@Override
	public String getDisplayName() {
		return "Left Click";
	}

	@Override
	public Macro createMacro(Point location) {
		LeftClickMacro macro = new LeftClickMacro();
		macro.location = location;
		return macro;
	}

	/*
	 * ============== Macro ===========================================
	 */

	@Override
	public void setConfigurationMemento(String memento) {
		String[] coordinates = memento.split(",");
		int x = Integer.parseInt(coordinates[0]);
		int y = Integer.parseInt(coordinates[1]);
		this.location = new Point(x, y);
	}

	@Override
	public String getConfigurationMemento() {
		return String.valueOf(this.location.x) + ","
				+ String.valueOf(this.location.y);
	}

	@Override
	public Point getStartingMouseLocation() {
		return this.location;
	}

	@Override
	public void runMacro(MacroContext context) {
		context.mouseMove(this.location.x, this.location.y);
		context.mouseClick(InputEvent.BUTTON1_MASK);
	}

}