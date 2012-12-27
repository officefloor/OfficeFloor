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

import java.awt.Point;
import java.awt.event.InputEvent;

/**
 * {@link Macro} to drag content.
 * 
 * @author Daniel Sagenschneider
 */
public class DragMacro implements MacroSource, Macro {

	/**
	 * Position of item for dragging.
	 */
	private Point itemPosition;

	/**
	 * Target position to drag item.
	 */
	private Point targetPosition;

	/**
	 * Obtains the target location.
	 * 
	 * @return Target location.
	 */
	public Point getTargetLocation() {
		return this.targetPosition;
	}

	/*
	 * ====================== MacroSource ==============================
	 */

	@Override
	public String getDisplayName() {
		return "Drag";
	}

	@Override
	public void sourceMacro(final MacroSourceContext context) {

		// Obtain the location of the item to drag
		final Point itemLocation = context.getLocation();

		// Create runnable to obtain another location and add macro
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Obtain the target location
				Point targetLocation = context.getAnotherLocation();

				// Create the macro
				DragMacro macro = new DragMacro();
				macro.itemPosition = itemLocation;
				macro.targetPosition = targetLocation;

				// Return the macro
				context.setNewMacro(macro);
			}
		};

		// Run to create macro
		new Thread(runnable).start();
	}

	/*
	 * ========================= Macro =================================
	 */

	@Override
	public String getConfigurationMemento() {
		// Return the location of item then target
		String memento = this.itemPosition.x + "," + this.itemPosition.y + "-"
				+ this.targetPosition.x + "," + this.targetPosition.y;
		return memento;
	}

	@Override
	public void setConfigurationMemento(String memento) {
		// Parse the memento into item and target positions
		String[] positions = memento.split("-");

		// Obtain the item position
		String itemText = positions[0];
		String[] itemCoordinates = itemText.split(",");
		int itemX = Integer.parseInt(itemCoordinates[0]);
		int itemY = Integer.parseInt(itemCoordinates[1]);
		this.itemPosition = new Point(itemX, itemY);

		// Obtain the target position
		String targetText = positions[1];
		String[] targetCoordinates = targetText.split(",");
		int targetX = Integer.parseInt(targetCoordinates[0]);
		int targetY = Integer.parseInt(targetCoordinates[1]);
		this.targetPosition = new Point(targetX, targetY);
	}

	@Override
	public String getDisplayLabel() {
		return "Drag (" + this.itemPosition.x + "," + this.itemPosition.y
				+ " - " + this.targetPosition.x + "," + this.targetPosition.y
				+ ")";
	}

	@Override
	public Point getStartingMouseLocation() {
		return this.itemPosition;
	}

	@Override
	public MacroTask[] getMacroTasks() {
		// Return tasks to drag item
		return new MacroTask[] { new MouseClickMacroTask(true),
				new MouseMoveMacroTask(this.targetPosition),
				new MouseClickMacroTask(false) };
	}

	/**
	 * {@link MacroTask} for a mouse click of the drag.
	 */
	private static class MouseClickMacroTask implements MacroTask {

		/**
		 * Flag indicating if press button. <code>false</code> indicates to
		 * release button.
		 */
		private final boolean isPressNotRelease;

		/**
		 * Initiate.
		 * 
		 * @param isPressNotRelease
		 *            Flag indicating if press button. <code>false</code>
		 *            indicates to release button.
		 */
		public MouseClickMacroTask(boolean isPressNotRelease) {
			this.isPressNotRelease = isPressNotRelease;
		}

		/*
		 * ================ MacroTask =============================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			if (this.isPressNotRelease) {
				// Press the left button
				context.mousePress(InputEvent.BUTTON1_MASK);
			} else {
				// Release the left button
				context.mouseRelease(InputEvent.BUTTON1_MASK);
			}
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

	/**
	 * {@link MacroTask} for mouse move of the drag.
	 */
	private static class MouseMoveMacroTask implements MacroTask {

		/**
		 * Target position to move mouse.
		 */
		private final Point targetPosition;

		/**
		 * Initiate.
		 * 
		 * @param targetPosition
		 *            Target position to move mouse.
		 */
		public MouseMoveMacroTask(Point targetPosition) {
			this.targetPosition = targetPosition;
		}

		/*
		 * ==================== MacroTask ========================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			// Move the mouse
			context.mouseMove(this.targetPosition.x, this.targetPosition.y);
		}

		@Override
		public long getPostRunWaitTime() {
			return 0;
		}
	}

}