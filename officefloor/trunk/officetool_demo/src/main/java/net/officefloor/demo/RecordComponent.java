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
package net.officefloor.demo;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroContext;
import net.officefloor.demo.macro.MacroFactory;

/**
 * Component that records actions sending them to {@link RecordListener}.
 *
 * @author Daniel Sagenschneider
 */
public class RecordComponent extends JComponent {

	/**
	 * {@link Toolkit}.
	 */
	private final Toolkit toolKit = Toolkit.getDefaultToolkit();

	/**
	 * {@link JFrame} containing this {@link RecordComponent}.
	 */
	private final JFrame frame;

	/**
	 * {@link JPopupMenu} to {@link Macro}.
	 */
	private final JPopupMenu popupMenu = new JPopupMenu();

	/**
	 * {@link RecordListener}.
	 */
	private final RecordListener recordListener;

	/**
	 * {@link Robot} to aid in triggering recording actions to allow showing
	 * results for further recording of actions.
	 */
	private final Robot robot;

	/**
	 * Background image to mimic transparency.
	 */
	private Image backgroundImage;

	/**
	 * Location of the mouse for a new {@link Macro}.
	 */
	private Point mouseLocationForNewMacro;

	/**
	 * Initiate.
	 *
	 * @param frame
	 *            {@link JFrame} containing this {@link RecordComponent}.
	 * @param recordListener
	 *            {@link RecordListener}.
	 * @throws AWTException
	 *             If fails to create necessary AWT components.
	 */
	public RecordComponent(JFrame frame, RecordListener recordListener)
			throws AWTException {
		this.frame = frame;
		this.recordListener = recordListener;
		this.robot = new Robot();

		// Initiate for transparency
		this.updateBackgroundImage();
		this.frame.addComponentListener(new RecordComponentListener());
		this.frame.addWindowFocusListener(new RecordWindowFocusListener());

		// Initiate popup menu for recording macros
		this.addMouseListener(new RecordMouseListener());

		// Add macro to refresh display (not to be recorded)
		this.popupMenu.add(new MacroAction(new RefreshDisplayMacroFactory(),
				false));
	}

	/**
	 * Adds a {@link MacroFactory}.
	 *
	 * @param macroFactory
	 *            {@link MacroFactory}.
	 * @return {@link JMenuItem} for the {@link MacroFactory}.
	 */
	public JMenuItem addMacro(MacroFactory macroFactory) {
		// Always record added macros
		return this.popupMenu.add(new MacroAction(macroFactory, true));
	}

	/**
	 * Updates the background image to mimic transparency.
	 */
	private void updateBackgroundImage() {
		try {
			Dimension dim = this.toolKit.getScreenSize();
			this.backgroundImage = RecordComponent.this.robot
					.createScreenCapture(new Rectangle(0, 0, (int) dim
							.getWidth(), (int) dim.getHeight()));
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this.frame, ex.getClass()
					.getSimpleName()
					+ ": " + ex.getMessage(), "Transparency Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Repaints the {@link RecordComponent} should it be visible.
	 */
	private void refreshBackground() {
		if (this.isVisible() && RecordComponent.this.isVisible()) {
			repaint();
		}
	}

	/*
	 * ======================= JComponent =============================
	 */

	@Override
	public void paintComponent(Graphics g) {
		Point pos = this.getLocationOnScreen();
		Point offset = new Point(-pos.x, -pos.y);
		g.drawImage(this.backgroundImage, offset.x, offset.y, null);
	}

	/**
	 * {@link Action} for a {@link Macro}.
	 */
	private class MacroAction extends AbstractAction implements MacroContext {

		/**
		 * {@link MacroFactory} to create the {@link Macro} for this
		 * {@link Action}.
		 */
		private final MacroFactory macroFactory;

		/**
		 * Indicates if the {@link Macro} should be recorded.
		 */
		private final boolean isRecord;

		/**
		 * Location of the {@link RecordComponent} on the screen.
		 */
		private Point recordComponentScreenLocation = null;

		/**
		 * Initiate.
		 *
		 * @param macroFactory
		 *            {@link MacroFactory}.
		 * @param isRecord
		 *            Indicates if the {@link Macro} should be recorded.
		 */
		public MacroAction(MacroFactory macroFactory, boolean isRecord) {
			super(macroFactory.getDisplayName());
			this.macroFactory = macroFactory;
			this.isRecord = isRecord;
		}

		/**
		 * Translates the relative {@link Point} to an absolute {@link Point}.
		 *
		 * @param relativeLocation
		 *            Relative location.
		 * @return Absolute location.
		 */
		private Point translateRelativeToAbsolute(Point relativeLocation) {
			return new Point(this.recordComponentScreenLocation.x
					+ relativeLocation.x, this.recordComponentScreenLocation.y
					+ relativeLocation.y);
		}

		/**
		 * Translates the absolute {@link Point} to the relative {@link Point}.
		 *
		 * @param absoluateLocation
		 *            Absoluate location.
		 * @return Relative location.
		 */
		private Point translateAbsoluteToRelative(Point absoluateLocation) {
			return new Point(absoluateLocation.x
					- this.recordComponentScreenLocation.x, absoluateLocation.y
					- this.recordComponentScreenLocation.y);
		}

		/*
		 * ================ AbstractAction ======================
		 */

		@Override
		public synchronized void actionPerformed(ActionEvent e) {

			// Obtain the location of this record component
			this.recordComponentScreenLocation = RecordComponent.this
					.getLocationOnScreen();

			// Obtain the relative location of macro
			Point macroLocation = this
					.translateAbsoluteToRelative(RecordComponent.this.mouseLocationForNewMacro);

			// Create the macro
			final Macro macro = this.macroFactory.createMacro(macroLocation);

			// Hide window to run macro on what is behind window
			Point frameLocation = RecordComponent.this.frame.getLocation();
			RecordComponent.this.frame.setVisible(false);

			// Run the macro
			macro.runMacro(this);

			// Allow time for macro to complete
			try {
				Thread.sleep(100);
			} catch (InterruptedException ex) {
				// Ignore and carry on to make visible
			}

			// Show window after macro triggered
			RecordComponent.this.updateBackgroundImage();
			RecordComponent.this.frame.setVisible(true);
			RecordComponent.this.frame.setLocation(frameLocation);
			RecordComponent.this.refreshBackground();

			// Record the macro if recordable
			if (this.isRecord) {
				RecordComponent.this.recordListener.addMacro(macro);
			}
		}

		/*
		 * ==================== MacroContext ================================
		 */

		@Override
		public void mouseMove(int x, int y) {
			// Obtain absolute location
			Point absoluteLocation = this
					.translateRelativeToAbsolute(new Point(x, y));

			// Move to absolute location (immediately)
			RecordComponent.this.robot.mouseMove(absoluteLocation.x,
					absoluteLocation.y);
		}

		@Override
		public void mousePress(int buttons) {
			RecordComponent.this.robot.mousePress(buttons);
		}

		@Override
		public void mouseRelease(int buttons) {
			RecordComponent.this.robot.mouseRelease(buttons);
		}

		@Override
		public void mouseClick(int buttons) {
			this.mousePress(buttons);
			this.mouseRelease(buttons);
		}

		@Override
		public void mouseWheel(int wheelAmt) {
			RecordComponent.this.robot.mouseWheel(wheelAmt);
		}

		@Override
		public void keyPress(int keycode) {
			RecordComponent.this.robot.keyPress(keycode);
		}

		@Override
		public void keyRelease(int keycode) {
			RecordComponent.this.robot.keyRelease(keycode);
		}

		@Override
		public void keyStroke(int keycode) {
			this.keyPress(keycode);
			this.keyRelease(keycode);
		}
	}

	/**
	 * {@link MacroFactory} to refresh the display.
	 */
	private class RefreshDisplayMacroFactory implements MacroFactory, Macro {

		/*
		 * ======================= MacroFactory ============================
		 */

		@Override
		public String getDisplayName() {
			return "Refresh display";
		}

		@Override
		public Macro createMacro(Point location) {
			return this;
		}

		/*
		 * ============================ Macro ================================
		 */

		@Override
		public void setConfigurationMemento(String memento) {
			// Requires no configuration
		}

		@Override
		public String getConfigurationMemento() {
			return "";
		}

		@Override
		public Point getStartingMouseLocation() {
			return null;
		}

		@Override
		public void runMacro(MacroContext context) {
			// Do nothing as MacroAction does refresh
		}
	}

	/**
	 * {@link MouseListener} to trigger {@link Macro} instances for recording.
	 */
	private class RecordMouseListener extends MouseAdapter {

		/**
		 * Shows the popup menu.
		 */
		private void showPopupMenu(MouseEvent e) {
			if (e.isPopupTrigger()) {

				// Specify the absolute location of mouse event
				Point componentOffset = e.getComponent().getLocationOnScreen();
				RecordComponent.this.mouseLocationForNewMacro = new Point(
						componentOffset.x + e.getX(), componentOffset.y
								+ e.getY());

				// Bring up the popup menu
				RecordComponent.this.popupMenu.show(e.getComponent(), e.getX(),
						e.getY());
			}
		}

		/*
		 * ================= MouseAdapater ==================
		 */

		@Override
		public void mousePressed(MouseEvent e) {
			this.showPopupMenu(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			this.showPopupMenu(e);
		}
	}

	/**
	 * {@link ComponentListener} to handle refreshing transparency.
	 */
	private class RecordComponentListener implements ComponentListener {

		/*
		 * =============== ComponentListener ===========================
		 */

		@Override
		public void componentShown(ComponentEvent evt) {
			RecordComponent.this.repaint();
		}

		@Override
		public void componentResized(ComponentEvent evt) {
			RecordComponent.this.repaint();
		}

		@Override
		public void componentMoved(ComponentEvent evt) {
			RecordComponent.this.repaint();
		}

		@Override
		public void componentHidden(ComponentEvent evt) {
			// Do nothing
		}
	}

	/**
	 * {@link WindowFocusListener} to handle refreshing transparency.
	 */
	private class RecordWindowFocusListener implements WindowFocusListener {

		/*
		 * ================= WindowFocuseListener ====================
		 */

		@Override
		public void windowGainedFocus(WindowEvent evt) {
			RecordComponent.this.refreshBackground();
		}

		@Override
		public void windowLostFocus(WindowEvent evt) {
			RecordComponent.this.refreshBackground();
		}
	}

}