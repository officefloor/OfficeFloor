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

package net.officefloor.eclipse.tool.demo;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroSource;
import net.officefloor.demo.macro.MacroSourceContext;
import net.officefloor.demo.macro.MacroTask;
import net.officefloor.demo.macro.MacroTaskContext;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * {@link MacroSource} to source a {@link Macro} to move the active window (
 * {@link Shell}).
 * 
 * @author Daniel Sagenschneider
 */
public class MoveActiveShellMacro implements MacroSource, Macro {

	/**
	 * Height of the title bar.
	 */
	public static final int TITLE_BAR_HEIGHT = 20;

	/**
	 * Height of the menu bar.
	 */
	public static final int MENU_BAR_HEIGHT = 20;

	/**
	 * Offset into width for mouse to move the {@link Shell}.
	 */
	public static final int MOUSE_OFFSET_WIDTH = 30;

	/**
	 * Location to move the active {@link Shell}.
	 */
	private Point moveLocation;

	/*
	 * ========================= MacroSource =============================
	 */

	@Override
	public String getDisplayName() {
		return "Move active window";
	}

	@Override
	public void sourceMacro(MacroSourceContext context) {

		// Create the macro
		MoveActiveShellMacro macro = new MoveActiveShellMacro();
		macro.moveLocation = context.getLocation();

		// Return the macro
		context.setNewMacro(macro);
	}

	/*
	 * ========================= Macro =============================
	 */

	@Override
	public String getDisplayLabel() {
		return null;
	}

	@Override
	public Point getStartingMouseLocation() {
		// Only move if necessary (handled by tasks)
		return null;
	}

	@Override
	public void setConfigurationMemento(String memento) {
		String[] coordinates = memento.split(",");
		int x = Integer.parseInt(coordinates[0]);
		int y = Integer.parseInt(coordinates[1]);
		this.moveLocation = new Point(x, y);
	}

	@Override
	public String getConfigurationMemento() {
		return String.valueOf(this.moveLocation.x) + ","
				+ String.valueOf(this.moveLocation.y);
	}

	@Override
	public MacroTask[] getMacroTasks() {
		// Return macro tasks
		return new MacroTask[] { new MoveMouseToShellTitleBarMacroTask(),
				new MouseClickMacroTask(true),
				new MoveShellToLocationMacroTask(),
				new MouseClickMacroTask(false) };
	}

	/**
	 * Obtains the active {@link Shell} bounds for the {@link Display}.
	 * 
	 * @param display
	 *            {@link Display}.
	 * @return Active {@link Shell} bounds for the {@link Display}.
	 */
	private Rectangle getActiveShellBounds(final Display display) {

		// Handler to receive active shell bounds
		final Rectangle[] activeShellBounds = new Rectangle[1];

		// Must obtain active shell bounds on SWT thread
		display.syncExec(new Runnable() {
			@Override
			public void run() {

				// Obtain the active shell
				Shell activeShell = display.getActiveShell();

				// Obtain location and size of active shell
				org.eclipse.swt.graphics.Point location = activeShell
						.toDisplay(0, 0);
				org.eclipse.swt.graphics.Point size = activeShell.getSize();

				// Adjust for title bar
				int y = location.y - TITLE_BAR_HEIGHT;
				int height = size.y + TITLE_BAR_HEIGHT;

				// Adjust for menu bar if one
				Menu menuBar = activeShell.getMenuBar();
				if (menuBar != null) {
					y -= MENU_BAR_HEIGHT;
					height += MENU_BAR_HEIGHT;
				}

				// Specify the active shell bounds
				synchronized (activeShellBounds) {
					activeShellBounds[0] = new Rectangle(location.x, y, size.x,
							height);
				}
			}
		});

		// Return the active shell bounds
		synchronized (activeShellBounds) {
			return activeShellBounds[0];
		}
	}

	/**
	 * {@link MacroTask} to move the mouse to the {@link Shell} title bar.
	 */
	private class MoveMouseToShellTitleBarMacroTask implements MacroTask {

		@Override
		public void runMacroTask(MacroTaskContext context) {

			// Obtain the location of the active shell
			IWorkbench workbench = PlatformUI.getWorkbench();
			Display display = workbench.getDisplay();
			Rectangle bounds = MoveActiveShellMacro.this
					.getActiveShellBounds(display);

			// Obtain the location to move the shell
			int x = bounds.x + MOUSE_OFFSET_WIDTH;
			int y = bounds.y + (TITLE_BAR_HEIGHT / 2);

			// Obtain the relative location
			Point relativeLocation = context
					.getRelativeLocation(new Point(x, y));

			// Move to title bar
			context.mouseMove(relativeLocation.x, relativeLocation.y);
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

	/**
	 * {@link MacroTask} to press or release the left mouse button.
	 */
	private class MouseClickMacroTask implements MacroTask {

		/**
		 * Flag indicating if mouse press or release. <code>true</code>
		 * indicates press.
		 */
		private final boolean isPressNotRelease;

		/**
		 * Initiate.
		 * 
		 * @param isPressNotRelease
		 *            Flag indicating if mouse press or release.
		 *            <code>true</code> indicates press.
		 */
		public MouseClickMacroTask(boolean isPressNotRelease) {
			this.isPressNotRelease = isPressNotRelease;
		}

		/*
		 * ================== MacroTask ========================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			if (isPressNotRelease) {
				// Left mouse button press
				context.mousePress(InputEvent.BUTTON1_MASK);
			} else {
				// Left mouse button release
				context.mouseRelease(InputEvent.BUTTON1_MASK);
			}
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

	/**
	 * Moves the mouse (and subsequently the {@link Shell}) to the specified
	 * move location.
	 * 
	 * @author Daniel Sagenschneider
	 */
	public class MoveShellToLocationMacroTask implements MacroTask {

		@Override
		public void runMacroTask(MacroTaskContext context) {

			// Calculate the move location
			int x = MoveActiveShellMacro.this.moveLocation.x
					- MOUSE_OFFSET_WIDTH;
			int y = MoveActiveShellMacro.this.moveLocation.y
					- (TITLE_BAR_HEIGHT / 2);

			// Move the mouse (and subsequently the shell)
			context.mouseMove(x, y);
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

}