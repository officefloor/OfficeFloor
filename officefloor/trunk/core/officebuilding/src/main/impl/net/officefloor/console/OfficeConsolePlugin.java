/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2014 Daniel Sagenschneider
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
package net.officefloor.console;

import java.awt.Label;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.SwingWorker;

import sun.tools.jconsole.OfficeConsole;

import com.sun.tools.jconsole.JConsolePlugin;

/**
 * {@link JConsolePlugin} for the {@link OfficeConsole}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeConsolePlugin extends JConsolePlugin {

	/*
	 * ===================== JConsolePlugin =======================
	 */

	@Override
	public Map<String, JPanel> getTabs() {
		Map<String, JPanel> tabs = new HashMap<>();
		tabs.put("OfficeBuilding", new OfficePanel());
		return tabs;
	}

	@Override
	public SwingWorker<?, ?> newSwingWorker() {
		// No refreshing required
		return null;
	}

	/**
	 * {@link JPanel} for the {@link OfficeConsole}.
	 */
	private static class OfficePanel extends JPanel {

		/**
		 * Initiate.
		 */
		public OfficePanel() {
			this.add(new Label("TODO"));
		}
	}

}