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
package net.officefloor.demo.play;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroContext;

/**
 * Plays the specified {@link Macro} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroPlayer implements MacroContext {

	/**
	 * Obtains the key codes for the character.
	 * 
	 * @param character
	 *            Character.
	 * @return Key codes for the character.
	 * @throws UnsupportedOperationException
	 *             If unsupported character.
	 * 
	 * @see KeyEvent
	 */
	public static int[] getCharacterKeyCodes(char character)
			throws UnsupportedOperationException {
		if (('0' <= character) && (character <= '9')) {
			// Number
			return new int[] { character - '0' + KeyEvent.VK_0 };
		} else if (('a' <= character) && (character <= 'z')) {
			// Lower case character
			return new int[] { character - 'a' + KeyEvent.VK_A };
		} else if (('A' <= character) && (character <= 'Z')) {
			// Upper case character (requires shift + character)
			return new int[] { KeyEvent.VK_SHIFT,
					(character - 'A' + KeyEvent.VK_A) };
		} else {
			switch (character) {
			case ' ':
				return new int[] { KeyEvent.VK_SPACE };
			default:
				// Unknown character
				throw new UnsupportedOperationException(
						"Unsupported character '" + character + "'");
			}
		}
	}

	/**
	 * Minimum press/release delay. Allows processing to occur due to the
	 * press/release.
	 */
	private static final int MIN_PRESS_RELEASE_DELAY = 10;

	/**
	 * {@link Robot} to aid in playing.
	 */
	private final Robot robot;

	/**
	 * Location of the mouse. Not null after first {@link Macro} providing
	 * starting position.
	 */
	private Point mouseLocation = null;

	/**
	 * Indicates if a press/release delay is required.
	 */
	private final boolean isRequirePressReleaseDelay;

	/**
	 * Initiate.
	 * 
	 * @param delay
	 *            Delay in between events for the player. The higher the value
	 *            the slower the play.
	 * @throws AWTException
	 *             If fails to initialise for playing.
	 */
	public MacroPlayer(int delay) throws AWTException {
		this.robot = new Robot();
		this.robot.setAutoDelay(delay);

		// Determine if require press/release delay
		this.isRequirePressReleaseDelay = (delay < MIN_PRESS_RELEASE_DELAY);
	}

	/**
	 * Initiate allowing specifying the {@link Robot} to use.
	 * 
	 * @param robot
	 *            {@link Robot} to use for playing.
	 */
	public MacroPlayer(Robot robot) {
		this.robot = robot;

		// Always require press/release delay
		this.isRequirePressReleaseDelay = true;
	}

	/**
	 * Plays the {@link Macro} instances in the order provided.
	 * 
	 * @param macros
	 *            {@link Macro} instances to play.
	 */
	public void play(Macro[] macros) {

		// Iterate over the macros
		for (Macro macro : macros) {

			// Move mouse to starting location
			Point startLocation = macro.getStartingMouseLocation();
			if (startLocation != null) {
				this.mouseMove(startLocation.x, startLocation.y);
			}

			// Run the macro
			macro.runMacro(this);
		}
	}

	/**
	 * Moves the mouse from the start to the end.
	 * 
	 * @param start
	 *            Start location.
	 * @param end
	 *            End location.
	 */
	private void moveMouse(Point start, Point end) {

		// Move immediately to starting location
		this.robot.mouseMove(start.x, end.y);

		// Calculate distance from start to end
		int width = end.x - start.x;
		int height = end.y - start.y;
		if ((width == 0) && (height == 0)) {
			// Start and end same location so no need to move
			return;
		}
		int distance = (int) Math.sqrt(Math.abs(width * width)
				+ Math.abs(height * height));

		// Calculate increment per step
		float widthIncrement = width / (float) distance;
		float heightIncrement = height / (float) distance;

		// Move mouse for each step
		float xLocation = start.x;
		float yLocation = start.y;
		for (int i = 0; i < distance; i++) {

			// Obtain location for next step
			xLocation += widthIncrement;
			yLocation += heightIncrement;

			// Move mouse
			this.robot.mouseMove((int) xLocation, (int) yLocation);
		}

		// Ensure at end location
		this.robot.mouseMove(end.x, end.y);
	}

	/**
	 * Delays for the press/release.
	 */
	private void delayForPressRelease() {
		if (this.isRequirePressReleaseDelay) {
			this.robot.delay(MIN_PRESS_RELEASE_DELAY);
		}
	}

	/*
	 * =================== MacroContext ===============================
	 */

	@Override
	public void mouseMove(int x, int y) {
		if (this.mouseLocation == null) {
			// Mouse location not yet specified, start at location
			this.robot.mouseMove(x, y);
			this.mouseLocation = new Point(x, y);
		} else {
			// Move mouse to location to run macro
			Point targetLocation = new Point(x, y);
			this.moveMouse(this.mouseLocation, targetLocation);
			this.mouseLocation = targetLocation;
		}
	}

	@Override
	public void mousePress(int buttons) {
		this.robot.mousePress(buttons);
		this.delayForPressRelease();
	}

	@Override
	public void mouseRelease(int buttons) {
		this.robot.mouseRelease(buttons);
		this.delayForPressRelease();
	}

	@Override
	public void mouseClick(int buttons) {
		this.mousePress(buttons);
		this.mouseRelease(buttons);
	}

	@Override
	public void mouseWheel(int wheelAmt) {
		this.robot.mouseWheel(wheelAmt);
		this.delayForPressRelease();
	}

	@Override
	public void keyPress(int keycode) {
		this.robot.keyPress(keycode);
		this.delayForPressRelease();
	}

	@Override
	public void keyRelease(int keycode) {
		this.robot.keyRelease(keycode);
		this.delayForPressRelease();
	}

	@Override
	public void keyStroke(int keycode) {
		this.keyPress(keycode);
		this.keyRelease(keycode);
	}

	@Override
	public void keyText(String text) {
		// Iterate over the characters triggering them to be input
		for (int i = 0; i < text.length(); i++) {
			char character = text.charAt(i);

			// Obtain the key codes for the character
			int[] keyCodes = getCharacterKeyCodes(character);

			// Press keys to input character
			for (int k = 0; k < keyCodes.length; k++) {
				this.keyPress(keyCodes[k]);
			}

			// Release the keys in reverse order as character input
			for (int k = keyCodes.length - 1; k >= 0; k--) {
				this.keyRelease(keyCodes[k]);
			}
		}
	}

}