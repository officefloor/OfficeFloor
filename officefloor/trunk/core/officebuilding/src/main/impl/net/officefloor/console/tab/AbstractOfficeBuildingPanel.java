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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.officefloor.building.console.OfficeFloorConsoleMain.OfficeFloorConsoleMainErrorHandler;
import net.officefloor.building.manager.OfficeBuildingManagerMBean;
import net.officefloor.building.manager.OpenOfficeFloorConfiguration;
import net.officefloor.console.ConfigureOfficeFloor;
import net.officefloor.console.OfficeBuilding;

/**
 * Abstract {@link JPanel} for the {@link OfficeBuilding}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeBuildingPanel extends JPanel {

	/**
	 * {@link OfficeBuildingManagerMBean}.
	 */
	protected final OfficeBuildingManagerMBean officeBuildingManager;

	/**
	 * Initiate.
	 * 
	 * @param officeBuildingManager
	 *            {@link OfficeBuildingManagerMBean}.
	 */
	public AbstractOfficeBuildingPanel(
			OfficeBuildingManagerMBean officeBuildingManager) {
		this.officeBuildingManager = officeBuildingManager;

		// Setup the panel
		try {
			this.init();
		} catch (Exception ex) {
			this.handleError(ex);
		}
	}

	/**
	 * Initialises the {@link JPanel} with {@link Component} instances.
	 * 
	 * @throws Exception
	 *             If fails to load.
	 */
	protected abstract void init() throws Exception;

	/**
	 * Creates the default {@link OpenOfficeFloorConfiguration}.
	 * 
	 * @return {@link OpenOfficeFloorConfiguration}.
	 * @throws Exception
	 *             If fails to create the {@link OpenOfficeFloorConfiguration}.
	 */
	protected OpenOfficeFloorConfiguration createDefaultOpenOfficeFloorConfiguration()
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
	protected void handleError(Throwable cause) {

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
	protected void handleError(String errorMessage) {
		this.showDialog(errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Notifies the user of the message.
	 * 
	 * @param message
	 *            Message for the user.
	 */
	protected void notifyUser(String message) {
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
	protected void showDialog(String message, String title, int messageType) {

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
		JDialog dialog = pane.createDialog(AbstractOfficeBuildingPanel.this,
				title);
		dialog.setVisible(true);
	}

	/**
	 * Provides means to propagate an error message.
	 */
	protected static class ErrorMessageException extends RuntimeException {

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
	protected abstract class OfficeAction extends AbstractAction {

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
				AbstractOfficeBuildingPanel.this.handleError(ex.getMessage());

			} catch (Throwable ex) {
				// Handle failure
				AbstractOfficeBuildingPanel.this.handleError(ex);
			}
		}
	}

}