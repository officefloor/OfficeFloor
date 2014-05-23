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
package net.officefloor.console.tab;

import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.swing.JPanel;
import javax.swing.SwingWorker;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import sun.tools.jconsole.OfficeConsole;

import com.sun.tools.jconsole.JConsoleContext;
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

		// Obtain the JConsole context
		JConsoleContext context = this.getContext();

		// Obtain the OfficeBuilding Manager
		MBeanServerConnection connection = context.getMBeanServerConnection();
		OfficeBuildingManagerMBean officeBuildingManager = JMX.newMBeanProxy(
				connection,
				OfficeBuildingManager.getOfficeBuildingManagerObjectName(),
				OfficeBuildingManagerMBean.class);

		// Create the tabs
		Map<String, JPanel> tabs = new HashMap<>();
		OfficeBuildingManageTabPanel manageTab = new OfficeBuildingManageTabPanel(
				officeBuildingManager);
		tabs.put("OfficeBuilding", manageTab);
		tabs.put("Simple Deploy", new OfficeBuildingSimpleDeployTabPanel(
				officeBuildingManager));
		tabs.put("Advanced Deploy", new OfficeBuildingAdvancedDeployTabPanel(
				officeBuildingManager));

		// Return the tabs
		return tabs;
	}

	@Override
	public SwingWorker<?, ?> newSwingWorker() {
		// No refreshing required
		return null;
	}

}