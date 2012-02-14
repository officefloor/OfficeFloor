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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroTask;
import net.officefloor.demo.macro.MacroTaskContext;
import net.officefloor.demo.macro.WaitMacro;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link MacroPlayer}.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroPlayerTest extends OfficeFrameTestCase {

	/**
	 * {@link MacroPlayer} to test.
	 */
	private MacroPlayer player;

	/**
	 * {@link Robot} to aid in testing the player.
	 */
	private Robot robot;

	/**
	 * {@link JFrame} for testing.
	 */
	private JFrame frame;

	/**
	 * Location of the {@link JFrame}.
	 */
	private Point windowLocation;

	/**
	 * {@link WaitMacro}.
	 */
	private WaitMacro waitMacro = new WaitMacro();

	/**
	 * {@link JButton} to click.
	 */
	private JButton button;

	/**
	 * Flag indicating if the {@link JButton} was clicked.
	 */
	private boolean isButtonClicked = false;

	/**
	 * {@Link JTextField} to enter text (via key strokes).
	 */
	private JTextField text;

	/**
	 * Offset into {@link JFrame} to ensure in window.
	 */
	private static final int OFFSET = 50;

	@Override
	protected void setUp() throws Exception {

		// Initiate the robot
		this.robot = new Robot();
		this.robot.setAutoWaitForIdle(true);

		// Provide window to record mouse location
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.frame.setSize(300, 300);
		this.frame.setLayout(new BoxLayout(this.frame.getContentPane(),
				BoxLayout.Y_AXIS));

		// Add the button for clicking
		this.button = new JButton("Click");
		this.button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Flag the button clicked
				MacroPlayerTest.this.isButtonClicked = true;
			}
		});
		this.frame.add(this.button);

		// Ad the text for entering key strokes
		this.text = new JTextField();
		this.frame.add(this.text);

		// Show the window (giving sometime to display)
		this.frame.setVisible(true);
		this.robot.waitForIdle();
		Thread.sleep(100);

		// Ensure window is correctly sized
		assertTrue("Window must have more width",
				(this.frame.getSize().width > (OFFSET * 2)));
		assertTrue("Window must have more height",
				(this.frame.getSize().height > (OFFSET * 2)));

		// Create the player to test (offset by frame location)
		this.windowLocation = this.frame.getLocationOnScreen();
		this.player = new MacroPlayer(5, this.windowLocation);
	}

	@Override
	protected void tearDown() throws Exception {
		this.frame.dispose();
		this.robot.waitForIdle();
	}

	/**
	 * Ensure can run the {@link Macro} with no mouse location.
	 */
	@GuiTest
	public void testNoMouseLocation() {

		// Create the macro that obtains context
		SingleMacroTaskMock mock = new SingleMacroTaskMock();

		// Record obtaining the context
		mock.record_getStartingMouseLocation(null);
		mock.record_getAndRunMacroTasks(this.player);

		// Run the macro
		this.replayMockObjects();
		this.player.play(mock.macro, this.waitMacro);
		this.waitMacro.waitUntilRun();
		this.verifyMockObjects();
	}

	/**
	 * Ensure able to move mouse to starting location.
	 */
	@GuiTest
	public void testMouseStartLocation() throws Exception {

		// Obtain starting and ending location for mouse move
		final Point initLocation = new Point(OFFSET, OFFSET);
		final Point startLocation = new Point(this.frame.getSize().width
				- OFFSET, this.frame.getSize().height - OFFSET);

		// Create mock objects for testing
		SingleMacroTaskMock mock = new SingleMacroTaskMock();

		// Record actions
		mock.record_getStartingMouseLocation(startLocation);
		mock.record_getAndRunMacroTasks(this.player);

		// Ensure mouse at initial location
		this.robot.mouseMove(initLocation.x, initLocation.y);
		this.assertMousePosition(initLocation);

		// Run test
		this.replayMockObjects();
		this.player.play(mock.macro, this.waitMacro);
		this.waitMacro.waitUntilRun();
		this.verifyMockObjects();

		// Ensure mouse at macro starting location
		this.assertMousePosition(startLocation);
	}

	/**
	 * Tests moving the mouse for next {@link Macro}.
	 */
	@GuiTest
	public void testMouseMoveLocation() {

		// Obtain starting and ending location for mouse move
		final Point startLocation = new Point(OFFSET, OFFSET);
		final Point finishLocation = new Point(this.frame.getSize().width
				- OFFSET, this.frame.getSize().height - OFFSET);

		// Create mock objects for testing
		SingleMacroTaskMock initPosition = new SingleMacroTaskMock();
		SingleMacroTaskMock movePosition = new SingleMacroTaskMock();

		// Record actions
		initPosition.record_getStartingMouseLocation(startLocation);
		initPosition.record_getAndRunMacroTasks(this.player);
		movePosition.record_getStartingMouseLocation(finishLocation);
		movePosition.record_getAndRunMacroTasks(this.player);

		// Run test
		this.replayMockObjects();
		this.player
				.play(initPosition.macro, movePosition.macro, this.waitMacro);
		this.waitMacro.waitUntilRun();
		this.verifyMockObjects();

		// Ensure mouse at finishing location
		this.assertMousePosition(finishLocation);
	}

	/**
	 * Tests moving the mouse in negative direction for next {@link Macro}.
	 */
	@GuiTest
	public void testMouseMoveLocationInNegativeDirection() {

		// Obtain starting and ending location for mouse move
		final Point startLocation = new Point(this.frame.getSize().width
				- OFFSET, this.frame.getSize().height - OFFSET);
		final Point finishLocation = new Point(OFFSET, OFFSET);

		// Create mock objects for testing
		SingleMacroTaskMock initPosition = new SingleMacroTaskMock();
		SingleMacroTaskMock movePosition = new SingleMacroTaskMock();

		// Record actions
		initPosition.record_getStartingMouseLocation(startLocation);
		initPosition.record_getAndRunMacroTasks(this.player);
		movePosition.record_getStartingMouseLocation(finishLocation);
		movePosition.record_getAndRunMacroTasks(this.player);

		// Run test
		this.replayMockObjects();
		this.player
				.play(initPosition.macro, movePosition.macro, this.waitMacro);
		this.waitMacro.waitUntilRun();
		this.verifyMockObjects();

		// Ensure mouse at finishing location
		this.assertMousePosition(finishLocation);
	}

	/**
	 * Ensure can trigger moving the mouse from the {@link MacroTaskContext}.
	 */
	@GuiTest
	public void testContext_mouseMove() {

		// Obtain starting and ending location for mouse move
		final Point startLocation = new Point(OFFSET, OFFSET);
		final Point finishLocation = new Point(this.frame.getSize().width
				- OFFSET, this.frame.getSize().height - OFFSET);

		// Ensure mouse at macro starting location
		this.robot.mouseMove(startLocation.x, startLocation.y);
		this.assertMousePosition(startLocation);

		// Move the mouse
		this.player.mouseMove(finishLocation.x, finishLocation.y);

		// Ensure mouse moved
		this.assertMousePosition(finishLocation);
	}

	/**
	 * Ensure able to trigger mouse click from the {@link MacroTaskContext}.
	 */
	@GuiTest
	public void testContext_mouseClick() throws Exception {

		// Move to button middle
		this.moveToComponentMiddle(this.button);

		// Trigger clicking the mouse
		this.player.mouseClick(InputEvent.BUTTON1_MASK);

		// Allow some time for registering key entered
		Thread.sleep(100);
		this.robot.waitForIdle();

		// Ensure that button was clicked
		assertTrue("Button should be clicked", this.isButtonClicked);
	}

	/**
	 * Ensure able to trigger key stroke.
	 */
	@GuiTest
	public void testContext_keyStroke() throws Exception {

		// Setup to input into the text
		this.moveToComponentMiddle(this.text);
		this.player.mouseClick(InputEvent.BUTTON1_MASK);

		// Enter the key stroke
		this.player.keyStroke(KeyEvent.VK_A);

		// Allow some time for registering key entered
		Thread.sleep(100);
		this.robot.waitForIdle();

		// Ensure key stroke entered
		String enteredKey = this.text.getText();
		assertEquals("Must enter key", 1, enteredKey.length());
		assertEquals("Incorrect key stroke", "a", enteredKey);
	}

	/**
	 * Ensure able to input text.
	 */
	@GuiTest
	public void testContext_keyText() {

		final String INPUT_TEXT = "Example Text with special characters ./:";

		// Setup to input into the text
		this.moveToComponentMiddle(this.text);
		this.player.mouseClick(InputEvent.BUTTON1_MASK);

		// Enter the key stroke
		this.player.keyText(INPUT_TEXT);

		// Ensure key stroke entered
		assertEquals("Incorrect key stroke", INPUT_TEXT, this.text.getText());
	}

	/**
	 * Ensure correctly obtains absolute location.
	 */
	@GuiTest
	public void testContext_AbsoluteLocation() {

		final Point relativeLocation = new Point(10, 10);

		// Calculate the absolute location
		final Point absoluteLocation = new Point(this.windowLocation.x
				+ relativeLocation.x, this.windowLocation.y
				+ relativeLocation.y);

		// Verify absolute location
		Point location = this.player.getAbsoluteLocation(relativeLocation);
		assertEquals("Incorrect absolute location", absoluteLocation, location);
	}

	/**
	 * Ensure correctly obtains relative location.
	 */
	@GuiTest
	public void testContext_RelativeLocation() {

		final Point absoluteLocation = new Point(10, 10);

		// Calculate the relative location
		final Point relativeLocation = new Point(absoluteLocation.x
				- this.windowLocation.x, absoluteLocation.y
				- this.windowLocation.y);

		// Verify relative location
		Point location = this.player.getRelativeLocation(absoluteLocation);
		assertEquals("Incorrect relative location", relativeLocation, location);
	}

	/**
	 * Moves the mouse to the middle of the {@link Component}.
	 * 
	 * @param component
	 *            {@link Component}.
	 */
	private void moveToComponentMiddle(Component component) {

		// Obtain the middle of the component
		Point middle = this.getComponentMiddle(component);

		// Move mouse to the middle
		this.robot.mouseMove(middle.x, middle.y);

		// Ensure the mouse at the middle
		this.assertMousePosition(middle);
	}

	/**
	 * Obtains the {@link Point} at the middle of the {@link Component}.
	 * 
	 * @param component
	 *            {@link Component}.
	 * @return {@link Point} at the middle of the {@link Component}.
	 */
	private Point getComponentMiddle(Component component) {

		// Obtain details of component
		Point location = component.getLocationOnScreen();
		Dimension size = component.getSize();

		// Determine middle location
		int middleX = location.x + (size.width / 2);
		int middleY = location.y + (size.height / 2);

		// Return the component middle
		return new Point(middleX, middleY);
	}

	/**
	 * Asserts that the mouse is at the location relative to the {@link JFrame}.
	 * 
	 * @param location
	 *            Location on the {@link JFrame}.
	 */
	private void assertMousePosition(Point location) {

		// Obtain the actual location of the mouse
		Point mouseLocation = this.frame.getMousePosition(true);
		assertNotNull(
				"No actual location (likely issue with window capturing it)",
				mouseLocation);

		// Obtain screen location of relative location
		Point screenLocation = new Point(location.x + this.windowLocation.x,
				location.y + this.windowLocation.y);

		// Provide slight margin of error in obtaining mouse location
		assertTrue("Incorrect x location (e=" + screenLocation.x + ", a="
				+ mouseLocation.x + ")",
				Math.abs(location.x - mouseLocation.x) <= 1);
		assertTrue("Incorrect y location (e=" + screenLocation.y + ", a="
				+ mouseLocation.y + ")",
				Math.abs(location.y - mouseLocation.y) <= 1);
	}

	/**
	 * Mocks for {@link Macro} and {@link MacroTask}.
	 */
	private class SingleMacroTaskMock {

		/**
		 * Mock {@link Macro}.
		 */
		public final Macro macro = MacroPlayerTest.this.createMock(Macro.class);

		/**
		 * Mock {@link MacroTask}.
		 */
		public final MacroTask task = MacroPlayerTest.this
				.createMock(MacroTask.class);

		/**
		 * Records obtaining the starting location.
		 * 
		 * @param startingLocation
		 *            Starting location.
		 */
		public void record_getStartingMouseLocation(Point startingLocation) {
			MacroPlayerTest.this.recordReturn(this.macro, this.macro
					.getStartingMouseLocation(), startingLocation);
		}

		/**
		 * Records obtaining the single {@link MacroTask}.
		 */
		public void record_getMacroTasks() {
			MacroPlayerTest.this.recordReturn(this.macro, this.macro
					.getMacroTasks(), new MacroTask[] { this.task });
		}

		/**
		 * Records obtaining the {@link MacroTask} instances and running them.
		 * 
		 * @param context
		 *            {@link MacroTaskContext}.
		 */
		public void record_getAndRunMacroTasks(MacroTaskContext context) {
			this.record_getMacroTasks();
			this.task.runMacroTask(context);
			MacroPlayerTest.this.recordReturn(this.task, this.task
					.getPostRunWaitTime(), 10);
		}
	}

}