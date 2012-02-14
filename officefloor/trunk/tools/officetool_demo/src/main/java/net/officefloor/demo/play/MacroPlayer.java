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

package net.officefloor.demo.play;

import java.awt.AWTException;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import javax.swing.SwingUtilities;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroTask;
import net.officefloor.demo.macro.MacroTaskContext;

/**
 * Plays the specified {@link Macro} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroPlayer implements MacroTaskContext {

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
			case '.':
				return new int[] { KeyEvent.VK_PERIOD };
			case '/':
				return new int[] { KeyEvent.VK_SLASH };
			case ':':
				return new int[] { KeyEvent.VK_SHIFT, KeyEvent.VK_COLON };
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
	 * Reference point for relative locations.
	 */
	private final Point offset;

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
	 * @param offset
	 *            Reference point for relative locations.
	 * @throws AWTException
	 *             If fails to initialise for playing.
	 */
	public MacroPlayer(int delay, Point offset) throws AWTException {
		this.robot = new Robot();
		this.robot.setAutoDelay(delay);
		this.offset = offset;

		// Determine if require press/release delay
		this.isRequirePressReleaseDelay = (delay < MIN_PRESS_RELEASE_DELAY);
	}

	/**
	 * Initiate allowing specifying the {@link Robot} to use.
	 * 
	 * @param robot
	 *            {@link Robot} to use for playing.
	 * @param offset
	 *            Reference point for relative locations.
	 */
	public MacroPlayer(Robot robot, Point offset) {
		this.robot = robot;
		this.offset = offset;

		// Always require press/release delay
		this.isRequirePressReleaseDelay = true;
	}

	/**
	 * Plays the {@link Macro} instances in the order provided.
	 * 
	 * @param macros
	 *            {@link Macro} instances to play.
	 */
	public void play(final Macro... macros) {

		// Run in own thread to allow event dispatch thread to update display
		Runnable player = new Runnable() {
			@Override
			public void run() {
				try {
					// Iterate over the macros
					for (final Macro macro : macros) {

						// Obtain the starting location
						final Point startLocation = macro
								.getStartingMouseLocation();

						// Run the macro tasks
						MacroTask[] tasks = macro.getMacroTasks();
						boolean isFirstTask = true;
						for (final MacroTask task : tasks) {

							// Wait until display idle before running task
							Thread.sleep(10); // allow some time for processing
							MacroPlayer.this.robot.waitForIdle();

							// Flag to determine if task run
							final boolean[] isRun = new boolean[1];
							isRun[0] = false;

							// Flag indicating if need to move to start location
							final boolean isMoveToStartLocation = isFirstTask;
							isFirstTask = false; // no longer first task

							// Run the task on the event dispatcher thread
							SwingUtilities.invokeAndWait(new Runnable() {
								@Override
								public void run() {
									try {
										if (isMoveToStartLocation) {
											// Move mouse to starting location
											if (startLocation != null) {
												MacroPlayer.this.mouseMove(
														startLocation.x,
														startLocation.y);
											}
										}

										// Run the task
										task.runMacroTask(MacroPlayer.this);

									} finally {
										// Flag task now run
										synchronized (isRun) {
											isRun[0] = true;
											isRun.notify();
										}
									}
								}
							});

							// Wait task is run
							synchronized (isRun) {
								while (!isRun[0]) {
									isRun.wait(100);
								}
							}

							// Wait for display to refresh
							Thread.sleep(10); // allow some time for processing
							MacroPlayer.this.robot.waitForIdle();

							// Wait specified time for task
							long taskWaitTime = task.getPostRunWaitTime();
							if (taskWaitTime > 0) {
								// Sleep the specified time
								Thread.sleep(taskWaitTime);
							}
						}
					}
				} catch (Throwable ex) {
					// Indicate failure in playing
					System.err.println("Failed playing macros");
					ex.printStackTrace();
				}
			}
		};

		// Run the player
		new Thread(player).start();

		// Allow calling thread (event dispatcher) to return
	}

	/**
	 * Moves the mouse from the start to the end.
	 * 
	 * @param start
	 *            Absolute start location - location on screen.
	 * @param end
	 *            Absolute end location - location on screen.
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

		// Obtain the absolute location
		Point location = this.getAbsoluteLocation(new Point(x, y));

		// Move the mouse
		if (this.mouseLocation == null) {
			// Mouse location not yet specified, start at location
			this.robot.mouseMove(location.x, location.y);
			this.mouseLocation = location;
		} else {
			// Move mouse to location to run macro
			this.moveMouse(this.mouseLocation, location);
			this.mouseLocation = location;
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

	@Override
	public Point getAbsoluteLocation(Point relativeLocation) {
		return new Point(this.offset.x + relativeLocation.x, this.offset.y
				+ relativeLocation.y);
	}

	@Override
	public Point getRelativeLocation(Point absoluteLocation) {
		return new Point(absoluteLocation.x - this.offset.x, absoluteLocation.y
				- this.offset.y);
	}

}