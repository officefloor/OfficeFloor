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

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.manager.UploadArtifact;
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

		// Create the tabs
		Map<String, JPanel> tabs = new HashMap<>();
		tabs.put("OfficeBuilding", new OfficePanel(context));

		// Return the tabs
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
		 * {@link JConsoleContext}.
		 */
		private final JConsoleContext context;

		/**
		 * {@link OfficeBuildingManagerMBean}.
		 */
		private final OfficeBuildingManagerMBean officeBuildingManager;

		/**
		 * Initiate.
		 * 
		 * @param context
		 *            {@link JConsoleContext}.
		 */
		public OfficePanel(JConsoleContext context) {
			this.context = context;

			// Obtain the OfficeBuilding Manager
			MBeanServerConnection connection = this.context
					.getMBeanServerConnection();
			this.officeBuildingManager = JMX.newMBeanProxy(connection,
					OfficeBuildingManager.getOfficeBuildingManagerObjectName(),
					OfficeBuildingManagerMBean.class);

			// Provide layout manager
			this.setLayout(new GridLayout(3, 1));

			// Provide header for errors
			JPanel panelError = (JPanel) this.add(new JPanel());
			final JLabel labelError = (JLabel) panelError.add(new JLabel());
			labelError.setForeground(Color.RED);

			// Setup the panel
			try {

				// Detail the host/port
				JPanel panelHostPort = (JPanel) this.add(new JPanel());
				String hostname = this.officeBuildingManager
						.getOfficeBuildingHostName();
				int port = this.officeBuildingManager.getOfficeBuildingPort();
				panelHostPort.add(new Label("Connected to " + hostname + ":"
						+ port));

				// Provide list of running processes
				String[] existingProcesses = this.officeBuildingManager
						.listProcessNamespaces();
				// TODO display within table (with buttons to stop, trigger
				// tasks)

				// Simple start
				this.add(this.createSimpleStartPanel(labelError));

				// Advanced start OfficeFloor
				this.add(this.createAdvancedStartPanel(labelError));

			} catch (Exception ex) {
				// Provide the failure
				labelError.setText("FAILURE: " + ex.getMessage() + " ["
						+ ex.getClass().getName() + "]");
			}
		}

		/**
		 * Creates the simple start {@link JPanel}.
		 * 
		 * @param labelError
		 *            To write errors.
		 * @return Simple start {@link JPanel}.
		 */
		private JPanel createSimpleStartPanel(final JLabel labelError) {

			// Simple start panel
			JPanel panelSimpleStart = new JPanel();

			// Select application file
			panelSimpleStart.add(new Label("Application file"));
			final JTextField textFileName = (JTextField) panelSimpleStart
					.add(new JTextField(20));
			panelSimpleStart.add(new JButton(new AbstractAction("...") {
				@Override
				public void actionPerformed(ActionEvent e) {
					JFileChooser fileChooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter(
							"Applications", "jar", "war");
					fileChooser.setFileFilter(filter);
					int returnVal = fileChooser
							.showOpenDialog(OfficePanel.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						textFileName.setText(fileChooser.getSelectedFile()
								.getAbsolutePath());
					}
				}
			}));

			// Start button
			panelSimpleStart.add(new JButton(new AbstractAction("start") {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Obtain the OfficeFloor location
					String officeFloorLocation = null;

					// Create the OfficeFloor configuration
					OpenOfficeFloorConfiguration configuration = new OpenOfficeFloorConfiguration(
							officeFloorLocation);

					// Load the simple file
					String artifactFilePath = textFileName.getText();
					if ((artifactFilePath == null)
							|| (artifactFilePath.trim().length() == 0)) {
						labelError.setText("Please select a file");
						return;
					}

					// Configure the upload artifact
					try {
						UploadArtifact uploadArtifact = new UploadArtifact(
								new File(artifactFilePath));
						configuration.addUploadArtifact(uploadArtifact);
					} catch (IOException ex) {
						labelError.setText("FAILURE: " + ex.getMessage() + " ("
								+ ex.getClass().getName() + ")");
					}

					// Start the OfficeFloor
					String processName;
					try {
						processName = OfficePanel.this.officeBuildingManager
								.openOfficeFloor(configuration);
					} catch (Exception ex) {
						// Provide failure
						labelError.setText("FAILURE opening: "
								+ ex.getMessage() + " ["
								+ ex.getClass().getName() + "]");
						return;
					}

					// Provide notification that opened
					labelError.setText("TODO: notify opened: " + processName);
				}
			}));

			// Return the simple start panel
			return panelSimpleStart;
		}

		/**
		 * Creates the advanced start {@link JPanel}.
		 * 
		 * @param labelError
		 *            To write errors.
		 * @return Advanced start {@link JPanel}.
		 */
		private JPanel createAdvancedStartPanel(JLabel labelError) {

			// Simple advanced panel
			JPanel panelAdvancedStart = new JPanel();

			panelAdvancedStart.add(new Label("Application file"));

			// Return the advanced start panel
			return panelAdvancedStart;
		}

	}

}