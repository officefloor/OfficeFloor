/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.demo.record;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.Frame;
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
import net.officefloor.demo.macro.MacroFactory;
import net.officefloor.demo.macro.MacroFactoryContext;
import net.officefloor.demo.macro.MacroTask;
import net.officefloor.demo.macro.MacroTaskContext;
import net.officefloor.demo.play.MacroPlayer;

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
	 * @param robot
	 *            {@link Robot}.
	 * @param frame
	 *            {@link JFrame} containing this {@link RecordComponent}.
	 * @param recordListener
	 *            {@link RecordListener}.
	 * @throws AWTException
	 *             If fails to create necessary AWT components.
	 */
	public RecordComponent(Robot robot, JFrame frame,
			RecordListener recordListener) throws AWTException {
		this.frame = frame;
		this.recordListener = recordListener;
		this.robot = robot;

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
	 * Obtains the {@link Macro} to hide the {@link JFrame}.
	 * 
	 * @return {@link Macro} to hide the {@link JFrame}.
	 */
	public Macro getHideFrameMacro() {
		return new HideFrameMacro();
	}

	/**
	 * Obtains the {@link Macro} to show the {@link JFrame}.
	 * 
	 * @return {@link Macro} to show the {@link JFrame}.
	 */
	public Macro getShowFrameMacro() {

		// Obtain location of frame for showing again after hiding
		Point frameLocation = RecordComponent.this.frame.getLocation();

		// Return the macro to show the frame
		return new ShowFrameMacro(frameLocation);
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
		if (this.isVisible()) {
			this.repaint();
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
	private class MacroAction extends AbstractAction implements
			MacroTaskContext, MacroFactoryContext {

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
		 * {@link Point} by which to obtain relative locations.
		 */
		private Point referencePoint = null;

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
		 * Obtains the {@link Point} by which to obtain relative locations.
		 * 
		 * @return {@link Point} by which to obtain relative locations.
		 */
		private Point getReferencePoint() {

			// If window visible, reset reference point
			if (RecordComponent.this.frame.isVisible()) {
				this.referencePoint = RecordComponent.this.frame
						.getLocationOnScreen();
			}

			// Return the reference point
			return this.referencePoint;
		}

		/**
		 * Obtains the {@link MacroPlayer} ready for immediate use.
		 * 
		 * @return {@link MacroPlayer}.
		 */
		private MacroPlayer getMacroPlayer() {

			// Obtain the reference point
			Point offset = this.getReferencePoint();

			// Return the player
			return new MacroPlayer(RecordComponent.this.robot, offset);
		}

		/*
		 * ================ AbstractAction ======================
		 */

		@Override
		public synchronized void actionPerformed(ActionEvent e) {
			try {

				// Create the macro
				final Macro macro = this.macroFactory.createMacro(this);

				// Obtain location of frame for showing again after hiding
				Point frameLocation = RecordComponent.this.frame.getLocation();

				// Run the macro (hiding/showing frame)
				new MacroPlayer(RecordComponent.this.robot, frameLocation)
						.play(RecordComponent.this.getHideFrameMacro(), macro,
								new ShowFrameMacro(frameLocation));

				// Record the macro if required
				if (this.isRecord) {
					RecordComponent.this.recordListener.addMacro(macro);
				}

			} catch (Throwable ex) {
				System.err.println("Failed to add macro");
				ex.printStackTrace();
			}
		}

		/*
		 * ===================== MacroFactoryContext ==================
		 */

		@Override
		public Point getLocation() {
			return this
					.getRelativeLocation(RecordComponent.this.mouseLocationForNewMacro);
		}

		@Override
		public Point getAbsoluteLocation(Point relativeLocation) {
			Point offset = this.getReferencePoint();
			return new Point(offset.x + relativeLocation.x, offset.y
					+ relativeLocation.y);
		}

		@Override
		public Point getRelativeLocation(Point absoluteLocation) {
			Point offset = this.getReferencePoint();
			return new Point(absoluteLocation.x - offset.x, absoluteLocation.y
					- offset.y);
		}

		@Override
		public Frame getOwnerFrame() {
			return RecordComponent.this.frame;
		}

		/*
		 * ==================== MacroContext ================================
		 */

		@Override
		public void mouseMove(int x, int y) {
			this.getMacroPlayer().mouseMove(x, y);
		}

		@Override
		public void mousePress(int buttons) {
			this.getMacroPlayer().mousePress(buttons);
		}

		@Override
		public void mouseRelease(int buttons) {
			this.getMacroPlayer().mouseRelease(buttons);
		}

		@Override
		public void mouseClick(int buttons) {
			this.mousePress(buttons);
			this.mouseRelease(buttons);
		}

		@Override
		public void mouseWheel(int wheelAmt) {
			this.getMacroPlayer().mouseWheel(wheelAmt);
		}

		@Override
		public void keyPress(int keycode) {
			this.getMacroPlayer().keyPress(keycode);
		}

		@Override
		public void keyRelease(int keycode) {
			this.getMacroPlayer().keyRelease(keycode);
		}

		@Override
		public void keyStroke(int keycode) {
			this.keyPress(keycode);
			this.keyRelease(keycode);
		}

		@Override
		public void keyText(String text) {
			this.getMacroPlayer().keyText(text);
		}
	}

	/**
	 * {@link Macro} to hide the {@link JFrame}.
	 */
	private class HideFrameMacro implements Macro, MacroTask {

		/*
		 * ==================== Macro ==================================
		 */

		@Override
		public String getConfigurationMemento() {
			throw new IllegalStateException("Should not be storing");
		}

		@Override
		public void setConfigurationMemento(String memento) {
			throw new IllegalStateException("Should not be initiating");
		}

		@Override
		public Point getStartingMouseLocation() {
			return null;
		}

		@Override
		public MacroTask[] getMacroTasks() {
			return new MacroTask[] { this };
		}

		/*
		 * ===================== MacroTask =============================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			// Hide the frame
			RecordComponent.this.frame.setVisible(false);
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

	/**
	 * {@link Macro} to show the {@link JFrame}.
	 */
	private class ShowFrameMacro implements Macro, MacroTask {

		/**
		 * Location to show the {@link JFrame}.
		 */
		private final Point frameLocation;

		/**
		 * Initiate.
		 * 
		 * @param frameLocation
		 *            Location to show the {@link JFrame}.
		 */
		public ShowFrameMacro(Point frameLocation) {
			this.frameLocation = frameLocation;
		}

		/*
		 * ==================== Macro ==================================
		 */

		@Override
		public String getConfigurationMemento() {
			throw new IllegalStateException("Should not be storing");
		}

		@Override
		public void setConfigurationMemento(String memento) {
			throw new IllegalStateException("Should not be initiating");
		}

		@Override
		public Point getStartingMouseLocation() {
			return null;
		}

		@Override
		public MacroTask[] getMacroTasks() {
			return new MacroTask[] { this };
		}

		/*
		 * ===================== MacroTask =============================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			// Show the frame
			RecordComponent.this.updateBackgroundImage();
			RecordComponent.this.frame.setVisible(true);
			RecordComponent.this.frame.setLocation(this.frameLocation);
			RecordComponent.this.refreshBackground();
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

	/**
	 * {@link MacroFactory} to refresh the display.
	 */
	private class RefreshDisplayMacroFactory implements MacroFactory, Macro,
			MacroTask {

		/*
		 * ======================= MacroFactory ============================
		 */

		@Override
		public String getDisplayName() {
			return "Refresh display";
		}

		@Override
		public Macro createMacro(MacroFactoryContext context) {
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
		public MacroTask[] getMacroTasks() {
			return new MacroTask[] { this };
		}

		/*
		 * =================== MacroTask ================================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			// Do nothing as MacroAction does refresh
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
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