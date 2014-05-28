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

import javax.swing.JOptionPane;
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
	 * {@link OfficeTablePanel} displaying the processes.
	 */
	private OfficeTablePanel processesTable;

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

	/**
	 * Refreshes the {@link OfficeFloor} processes.
	 */
	public void refreshOfficeFloorProcesses() throws Exception {

		// Run the refresh within an asynchronous action
		this.doAsyncAction(new OfficeAsyncAction<String[]>(
				"Refreshing OfficeFloor processes") {
			@Override
			public String[] doAction() throws Exception {

				// Obtain the existing processes
				String[] existingProcesses = OfficeBuildingManageTabPanel.this.officeBuildingManager
						.listProcessNamespaces();

				// Return the existing processes
				return existingProcesses;
			}

			@Override
			public void done(String[] result) throws Exception {
				// Remove all the rows
				int rowCount = OfficeBuildingManageTabPanel.this.processesTable
						.getRows().size();
				for (int i = 0; i < rowCount; i++) {
					OfficeBuildingManageTabPanel.this.processesTable
							.removeRow(0);
				}

				// Add the refreshed OfficeFloor processes
				for (String existingProcess : result) {
					OfficeBuildingManageTabPanel.this.processesTable
							.addRow(existingProcess);
				}
			}
		});
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
		this.processesTable = new ProcessTablePanel();
		this.add(this.processesTable);

		// Load processes to table
		this.refreshOfficeFloorProcesses();
	}

	/**
	 * {@link OfficeTablePanel} for the {@link OfficeFloor} processes.
	 */
	private class ProcessTablePanel extends OfficeTablePanel {

		/**
		 * Initiate.
		 */
		public ProcessTablePanel() {
			super(false, false, "Processes");
		}

		/*
		 * ================ ProcessTablePanel ======================
		 */

		@Override
		protected void handleDeleteRow(final int rowIndex) {
			try {
				// Obtain the process to close
				String[] row = this.getRows().get(rowIndex);
				final String processNameSpace = row[0];

				// Confirm want to close OfficeFloor
				int result = JOptionPane.showConfirmDialog(
						OfficeBuildingManageTabPanel.this,
						"Please confirm closing " + processNameSpace, "Close",
						JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return; // Not confirmed
				}

				// Close the OfficeFloor within an asynchronous action
				OfficeBuildingManageTabPanel.this
						.doAsyncAction(new OfficeAsyncAction<Void>(
								"Close OfficeFloor") {
							@Override
							public Void doAction() throws Exception {
								// Close the OfficeFloor
								final int waitTime = 10 * 1000; // 10 seconds
								OfficeBuildingManageTabPanel.this.officeBuildingManager
										.closeOfficeFloor(processNameSpace,
												waitTime);

								// Nothing to return
								return null;
							}

							@Override
							public void done(Void result) throws Exception {
								// Remove the row
								ProcessTablePanel.super
										.handleDeleteRow(rowIndex);
							}
						});

			} catch (Exception ex) {
				OfficeBuildingManageTabPanel.this.handleError(ex);
			}
		}
	}

}