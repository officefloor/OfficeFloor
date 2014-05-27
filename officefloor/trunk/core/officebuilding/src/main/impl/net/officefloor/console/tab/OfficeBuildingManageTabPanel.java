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

import java.awt.Label;

import javax.swing.JPanel;

import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link JPanel} for the managing the running {@link OfficeFloor} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingManageTabPanel extends AbstractOfficeBuildingPanel {

	/**
	 * Initiate.
	 * 
	 * @param officeBuildingManager
	 *            {@link OfficeBuildingManagerMBean}.
	 */
	public OfficeBuildingManageTabPanel(
			OfficeBuildingManagerMBean officeBuildingManager) {
		super(officeBuildingManager);
	}

	/*
	 * ================= AbstractOfficeBuildingPanel ================
	 */

	@Override
	protected void init() throws Exception {

		// Detail the host/port
		JPanel panelHostPort = (JPanel) new JPanel();
		this.add(panelHostPort);
		String hostname = this.officeBuildingManager
				.getOfficeBuildingHostName();
		int port = this.officeBuildingManager.getOfficeBuildingPort();
		panelHostPort.add(new Label("Connected to " + hostname + ":" + port));

		// Provide list of running processes
		String[] existingProcesses = this.officeBuildingManager
				.listProcessNamespaces();
		OfficeTablePanel table = new OfficeTablePanel(false, false, "Process");
		for (String existingProcess : existingProcesses) {
			table.addRow(existingProcess);
		}
		this.add(table);
	}

}