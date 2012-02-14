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

import java.awt.Color;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;
import javax.swing.JTextArea;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.border.LineBorder;

/**
 * {@link Macro} to display information.
 * 
 * @author Daniel Sagenschneider
 */
public class InfoMacro implements MacroSource, Macro {

	/**
	 * Location for the {@link Macro}.
	 */
	private Point macroLocation = null;

	/**
	 * Text information for the {@link Macro}.
	 */
	private String macroInfoText = null;

	/**
	 * Obtains the location to display the information.
	 * 
	 * @return Location to display the information.
	 */
	public Point getInfoLocation() {
		return this.macroLocation;
	}

	/**
	 * Obtains the information text.
	 * 
	 * @return Information text.
	 */
	public String getInfoText() {
		return this.macroInfoText;
	}

	/*
	 * ================= MacroSource =============================
	 */

	@Override
	public String getDisplayName() {
		return "Info";
	}

	@Override
	public void sourceMacro(MacroSourceContext context) {

		// Obtain location for dialog
		Point relativeLocation = context.getLocation();
		Point initialLocation = context.getAbsoluteLocation(relativeLocation);

		// Obtain the information
		InfoDialog dialog = new InfoDialog(context.getOwnerFrame(),
				initialLocation);
		dialog.setVisible(true);

		// Obtain the relative location
		Point infoAbsoluteLocation = dialog.getInfoLocation();
		Point infoRelativeLocation = context
				.getRelativeLocation(infoAbsoluteLocation);

		// Obtain the information
		String infoText = dialog.getInfoText();

		// Create the macro
		InfoMacro macro = new InfoMacro();
		macro.macroLocation = infoRelativeLocation;
		macro.macroInfoText = infoText;

		// Return the macro
		context.setNewMacro(macro);
	}

	/*
	 * ====================== Macro ================================
	 */

	@Override
	public String getConfigurationMemento() {
		return this.macroLocation.x + "," + this.macroLocation.y + ":"
				+ this.macroInfoText;
	}

	@Override
	public void setConfigurationMemento(String memento) {
		// Parse out the configuration
		int split = memento.indexOf(':');

		// Obtain the location
		String location = memento.substring(0, split);
		String[] point = location.split(",");
		int x = Integer.parseInt(point[0]);
		int y = Integer.parseInt(point[1]);
		this.macroLocation = new Point(x, y);

		// Obtain the information text (+1 to skip separator)
		this.macroInfoText = memento.substring(split + 1);
	}

	@Override
	public String getDisplayLabel() {

		final int MAX_TEXT_LENGTH = 20;

		// Trim information for display
		String information = this.macroInfoText;
		information = (information.length() > MAX_TEXT_LENGTH ? information
				.substring(0, MAX_TEXT_LENGTH) : information);

		// Return the display label
		return "Info: " + information;
	}

	@Override
	public Point getStartingMouseLocation() {
		// No starting mouse location as only information to display
		return null;
	}

	@Override
	public MacroTask[] getMacroTasks() {

		// Create the tasks
		ShowInfoMacroTask showTask = new ShowInfoMacroTask(this.macroLocation,
				this.macroInfoText);
		HideInfoMacroTask hideTask = new HideInfoMacroTask(showTask);

		// Return the tasks
		return new MacroTask[] { showTask, hideTask };
	}

	/**
	 * Shows the information.
	 */
	private class ShowInfoMacroTask implements MacroTask {

		/**
		 * Relative location to display the information.
		 */
		private final Point location;

		/**
		 * Information to be displayed.
		 */
		private final String information;

		/**
		 * {@link Popup} to display the information.
		 */
		private Popup popup = null;

		/**
		 * Initiate.
		 * 
		 * @param location
		 *            Relative location to display the information.
		 * @param information
		 *            Information to be displayed.
		 */
		public ShowInfoMacroTask(Point location, String information) {
			this.location = location;
			this.information = information;
		}

		/**
		 * Hides the information.
		 */
		public synchronized void hideInformation() {
			this.popup.hide();
		}

		/*
		 * =================== MacroTask ===========================
		 */

		@Override
		public synchronized void runMacroTask(MacroTaskContext context) {

			// Create text area to display information
			JTextArea information = new JTextArea(this.information);
			information.setEditable(false);
			information.setBorder(new LineBorder(Color.LIGHT_GRAY));
			information.setBackground(new Color(255, 255, 180));

			// Create the popup to show the information
			final Point absoluteLocation = context
					.getAbsoluteLocation(this.location);
			this.popup = PopupFactory.getSharedInstance().getPopup(null,
					information, absoluteLocation.x, absoluteLocation.y);

			// Show the information
			this.popup.show();
		}

		@Override
		public long getPostRunWaitTime() {

			// Determine wait time based on words (with minimum of second)
			int wordCount = this.information.split(" ").length;
			long waitTime = Math.max(wordCount * 300, 1000);

			// Return wait time
			return waitTime;
		}
	}

	/**
	 * Hides the information.
	 */
	private class HideInfoMacroTask implements MacroTask {

		/**
		 * {@link ShowInfoMacroTask}.
		 */
		private final ShowInfoMacroTask showTask;

		/**
		 * Initiate.
		 * 
		 * @param showTask
		 *            {@link ShowInfoMacroTask}.
		 */
		public HideInfoMacroTask(ShowInfoMacroTask showTask) {
			this.showTask = showTask;
		}

		/*
		 * =================== MacroTask ================================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			this.showTask.hideInformation();
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

	/**
	 * {@link JDialog} to obtain the information.
	 */
	private class InfoDialog extends JDialog {

		/**
		 * {@link JTextArea} containing the information.
		 */
		private final JTextArea info;

		/**
		 * Location to display the information.
		 */
		private Point infoLocation = null;

		/**
		 * Initiate.
		 * 
		 * @param owner
		 *            Owner
		 * @param initialLocation
		 *            Initial location.
		 */
		public InfoDialog(Frame owner, Point initialLocation) {
			super(owner, "Input Information", true);

			// Flag to close
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

			// Information
			this.info = new JTextArea();
			this.info.setEditable(true);
			this.info.setText("");
			this.add(this.info);

			// Specify bounds
			this.setLocation(initialLocation);
			this.setSize(200, 100);

			// Retain bounds of window when closing
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {

					// Use the dialog location
					Point location = InfoDialog.this.getLocation();

					// Specify location
					InfoDialog.this.infoLocation = new Point(location);
				}
			});
		}

		/**
		 * Obtains the location to display the information.
		 * 
		 * @return Location to display the information.
		 */
		public Point getInfoLocation() {
			return this.infoLocation;
		}

		/**
		 * Obtains the information text.
		 * 
		 * @return Information text.
		 */
		public String getInfoText() {
			return this.info.getText();
		}
	}

}