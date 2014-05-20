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
package net.officefloor.console.command;

import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.officefloor.building.console.OfficeFloorConsoleMain.OfficeFloorConsoleMainErrorHandler;
import net.officefloor.building.manager.OfficeBuildingManager;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.building.manager.UploadArtifact;
import net.officefloor.console.ConfigureOfficeFloor;

import com.sun.tools.jconsole.JConsoleContext;

/**
 * {@link JPanel} for the {@link OfficeConsolePlugin}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeBuildingConsolePanel extends JPanel {

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
	public OfficeBuildingConsolePanel(JConsoleContext context) {
		this.context = context;

		// Obtain the OfficeBuilding Manager
		MBeanServerConnection connection = this.context
				.getMBeanServerConnection();
		this.officeBuildingManager = JMX.newMBeanProxy(connection,
				OfficeBuildingManager.getOfficeBuildingManagerObjectName(),
				OfficeBuildingManagerMBean.class);

		// Setup the panel
		try {

			// Provide layout manager
			this.setLayout(new GridLayout(3, 1));

			// Detail the host/port
			JPanel panelHostPort = (JPanel) this.add(new JPanel());
			String hostname = this.officeBuildingManager
					.getOfficeBuildingHostName();
			int port = this.officeBuildingManager.getOfficeBuildingPort();
			panelHostPort
					.add(new Label("Connected to " + hostname + ":" + port));

			// Provide list of running processes
			String[] existingProcesses = this.officeBuildingManager
					.listProcessNamespaces();
			// TODO display within table (with buttons to stop, trigger
			// tasks)

			// Simple start
			this.add(this.createSimpleStartPanel());

			// Advanced start OfficeFloor
			this.add(this.createAdvancedStartPanel());

		} catch (Exception ex) {
			this.handleError(ex);
		}
	}

	/**
	 * Creates the simple start {@link JPanel}.
	 * 
	 * @return Simple start {@link JPanel}.
	 */
	private JPanel createSimpleStartPanel() {

		// Simple start panel
		JPanel panelSimpleStart = new JPanel();

		// Select application file
		panelSimpleStart.add(new Label("Application file"));
		final JTextField textFileName = (JTextField) panelSimpleStart
				.add(new JTextField(20));
		panelSimpleStart.add(new JButton(new OfficeAction("...") {
			@Override
			public void doAction() {
				JFileChooser fileChooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
						"Applications", "jar", "war");
				fileChooser.setFileFilter(filter);
				int returnVal = fileChooser
						.showOpenDialog(OfficeBuildingConsolePanel.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					textFileName.setText(fileChooser.getSelectedFile()
							.getAbsolutePath());
				}
			}
		}));

		// Start button
		panelSimpleStart.add(new JButton(new OfficeAction("start") {
			@Override
			public void doAction() throws Exception {

				// Obtain default open OfficeFloor configuration
				OpenOfficeFloorConfiguration configuration = OfficeBuildingConsolePanel.this
						.createDefaultOpenOfficeFloorConfiguration();

				// Load the artifact
				String artifactFilePath = textFileName.getText();
				if ((artifactFilePath == null)
						|| (artifactFilePath.trim().length() == 0)) {
					throw new ErrorMessageException("Please select a file");
				}

				// Configure the upload artifact
				UploadArtifact uploadArtifact = new UploadArtifact(new File(
						artifactFilePath));
				configuration.addUploadArtifact(uploadArtifact);

				// Start the OfficeFloor
				String processName = OfficeBuildingConsolePanel.this.officeBuildingManager
						.openOfficeFloor(configuration);

				// Provide notification that opened
				OfficeBuildingConsolePanel.this
						.notifyUser("Opened under process name: " + processName);
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
	private JPanel createAdvancedStartPanel() throws Exception {

		// Advanced panel
		JPanel panelAdvancedStart = new JPanel();

		// Provide open OfficeFloor configuration
		OpenOfficeFloorConfiguration configuration = this
				.createDefaultOpenOfficeFloorConfiguration();

		// Configuration methods
		if (false) {
		configuration.setOfficeFloorSourceClassName(null); // text
		configuration.setOfficeFloorLocation(null); // text
		configuration.setProcessName(null); // text
		configuration.addArtifactReference(null); // list
		configuration.addClassPathEntry(null); // list
		configuration.addJvmOption(null); // list
		configuration.addOfficeFloorProperty(null, null); // table
		configuration.addRemoteRepositoryUrl(null); // list
		configuration.addUploadArtifact(null); // list (of file selections)
		configuration.setOpenTask(null, null, null, null); // text fields
		}

		// Return the advanced start panel
		return panelAdvancedStart;
	}

	/**
	 * Creates the default {@link OpenOfficeFloorConfiguration}.
	 * 
	 * @return {@link OpenOfficeFloorConfiguration}.
	 * @throws Exception
	 *             If fails to create the {@link OpenOfficeFloorConfiguration}.
	 */
	private OpenOfficeFloorConfiguration createDefaultOpenOfficeFloorConfiguration()
			throws Exception {

		// Create the open OfficeFloor configuration
		OpenOfficeFloorConfiguration configuration = ConfigureOfficeFloor
				.newOpenOfficeFloorConfiguration(new OfficeFloorConsoleMainErrorHandler() {
					@Override
					public void warning(String warning) {
						// Should be no warnings
						throw new ErrorMessageException(warning);
					}

					@Override
					public void errorAndExit(String... lines) {

						// Create the message
						StringBuilder message = new StringBuilder();
						for (String line : lines) {
							message.append(line);
							message.append("\n");
						}

						// Propagate the message
						throw new ErrorMessageException(message.toString());
					}
				});

		// Return the open OfficeFloor configuration
		return configuration;
	}

	/**
	 * Handles the error.
	 * 
	 * @param cause
	 *            Cause of the error.
	 */
	private void handleError(Throwable cause) {

		// Obtain the stack trace
		StringWriter stackTrace = new StringWriter();
		cause.printStackTrace(new PrintWriter(stackTrace, true));

		// Obtain the message
		StringBuilder message = new StringBuilder();
		message.append(cause.getMessage());
		message.append("\n\n");
		message.append(stackTrace.toString());

		// Handle the error
		this.handleError(message.toString());
	}

	/**
	 * Handles the error.
	 * 
	 * @param errorMessage
	 *            Error message.
	 */
	private void handleError(String errorMessage) {
		this.showDialog(errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Notifies the user of the message.
	 * 
	 * @param message
	 *            Message for the user.
	 */
	private void notifyUser(String message) {
		this.showDialog(message, "", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shows the {@link JDialog}.
	 * 
	 * @param message
	 *            Message.
	 * @param title
	 *            Title.
	 * @param messageType
	 *            {@link JOptionPane} message type.
	 */
	private void showDialog(String message, String title, int messageType) {

		// Determine appropriate dimensions for text area
		String[] lines = message.split("\n");

		// Height limited
		int height = Math.min(30, lines.length);

		// Width limited
		int maxLineWidth = 0;
		for (String line : lines) {
			int lineLength = line.length();
			if (maxLineWidth < lineLength) {
				maxLineWidth = lineLength;
			}
		}
		int width = Math.min(80, maxLineWidth);

		// Create the dialog to display the message
		JTextArea messageText = new JTextArea(height, width);
		messageText.setText(message);
		messageText.setEditable(false);
		messageText.setCaretPosition(0); // Start scroll at top
		JScrollPane scrollPane = new JScrollPane(messageText);
		JOptionPane pane = new JOptionPane(scrollPane, messageType);

		// Display the dialog
		JDialog dialog = pane.createDialog(OfficeBuildingConsolePanel.this,
				title);
		dialog.setVisible(true);
	}

	/**
	 * Provides means to propagate an error message.
	 */
	private static class ErrorMessageException extends RuntimeException {

		/**
		 * Initiate.
		 * 
		 * @param errorMessage
		 *            Error message.
		 */
		public ErrorMessageException(String errorMessage) {
			super(errorMessage);
		}
	}

	/**
	 * {@link AbstractAction} that handles failures.
	 */
	private abstract class OfficeAction extends AbstractAction {

		/**
		 * Initiate.
		 * 
		 * @param name
		 *            Name of {@link Action}.
		 */
		public OfficeAction(String name) {
			super(name);
		}

		/**
		 * Undertakes the {@link Action}.
		 * 
		 * @throws Exception
		 *             If {@link Action} fails.
		 */
		public abstract void doAction() throws Exception;

		/*
		 * ================== AbstractAction =================
		 */

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				// Undertake the action
				this.doAction();

			} catch (ErrorMessageException ex) {
				// Handle error message
				OfficeBuildingConsolePanel.this.handleError(ex.getMessage());

			} catch (Throwable ex) {
				// Handle failure
				OfficeBuildingConsolePanel.this.handleError(ex);
			}
		}
	}

}