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

package net.officefloor.demo.record;

import java.awt.Container;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Deque;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;

import net.officefloor.demo.macro.DragMacro;
import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroSource;
import net.officefloor.demo.macro.MacroSourceContext;
import net.officefloor.demo.macro.MacroTask;
import net.officefloor.demo.macro.MacroTaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Test the {@link RecordComponent}.
 * 
 * @author Daniel Sagenschneider
 */
public class RecordComponentTest extends OfficeFrameTestCase {

	/**
	 * {@link RecordComponent} being tested.
	 */
	private RecordComponent recordComponent;

	/**
	 * Mock {@link FrameVisibilityListener}.
	 */
	private final FrameVisibilityListener visibilityListener = this
			.createSynchronizedMock(FrameVisibilityListener.class);

	/**
	 * Mock {@link RecordListener}.
	 */
	private final MockRecordListener recordListener = new MockRecordListener();

	/**
	 * Offset location.
	 */
	private Point offsetLocation;

	/**
	 * {@link MockFrame}.
	 */
	private MockFrame mockFrame;

	/**
	 * {@link JFrame} containing the {@link RecordComponent}.
	 */
	private JFrame recordFrame;

	/**
	 * Potential failure of {@link Macro}.
	 */
	private volatile Throwable macroFailure = null;

	/**
	 * {@link Robot} to aid in unit testing.
	 */
	private Robot robot;

	/**
	 * Tests single click.
	 */
	@GuiTest
	public void testSingleClick() throws Throwable {
		final Point location = this
				.getRelativeMouseLocation(this.mockFrame.button);
		this.addExpectedMacro(LeftClickMacro.class, location, location.x + ","
				+ location.y);
		this.runMacro(new LeftClickMacro(), this.mockFrame.button);
		assertTrue("Should click button", this.mockFrame.isButtonClicked);
	}

	/**
	 * Test get another location with {@link DragMacro}.
	 */
	@GuiTest
	public void testAnotherLocation() throws Throwable {

		// Specify locations
		final Point itemAbsoluteLocation = getMouseLocation(this.mockFrame.button);
		final Point itemRelativeLocation = this
				.getRelativeMouseLocation(itemAbsoluteLocation);
		final Point targetAbsoluteLocation = new Point(
				itemAbsoluteLocation.x - 20, itemAbsoluteLocation.y - 20);
		final Point targetRelativeLocation = this
				.getRelativeMouseLocation(targetAbsoluteLocation);

		// Create the macro
		this.addExpectedMacro(DragMacro.class, itemRelativeLocation,
				itemRelativeLocation.x + "," + itemRelativeLocation.y + "-"
						+ targetRelativeLocation.x + ","
						+ targetRelativeLocation.y);
		this.runMacro(new DragMacro(), this.mockFrame.button);

		// Trigger click for another location
		this.robot
				.mouseMove(targetAbsoluteLocation.x, targetAbsoluteLocation.y);
		this.robot.mousePress(InputEvent.BUTTON1_MASK);
		this.robot.mouseRelease(InputEvent.BUTTON1_MASK);
		this.robot.waitForIdle();

		// Button should not be clicked
		assertFalse("Button should not be clicked",
				this.mockFrame.isButtonClicked);
	}

	/*
	 * ===================== TestCase ==================================
	 */

	@Override
	protected void setUp() throws Exception {

		// Record mock actions for recording
		this.visibilityListener.notifyFrameVisibility(false);
		this.visibilityListener.notifyFrameVisibility(true);
		this.replayMockObjects();

		// Create Robot to aid in testing
		this.robot = new Robot();
		this.robot.setAutoDelay(100); // allow invoking

		// Create the mock window first to be in background
		this.mockFrame = new MockFrame();
		this.mockFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.mockFrame.setLocation(100, 100);
		this.mockFrame.setSize(400, 400);
		this.mockFrame.setVisible(true);
		this.robot.waitForIdle(); // Wait for window to appear

		// Create window containing the record component in front
		this.recordFrame = new JFrame();
		this.recordFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.recordComponent = new RecordComponent(new Robot(),
				this.recordFrame, this.visibilityListener, this.recordListener);
		this.recordFrame.setTitle("Recorder");
		this.recordFrame.setLocation(100, 100);
		this.recordFrame.setSize(400, 400);
		this.recordFrame.getContentPane().add(this.recordComponent);
		this.recordFrame.setVisible(true);
		this.robot.waitForIdle(); // Wait for window to appear

		// Bring record to the front
		this.recordFrame.setVisible(true);
		this.robot.waitForIdle();

		// Obtain the offset location
		this.offsetLocation = this.recordComponent.getLocationOnScreen();
	}

	@Override
	protected void tearDown() throws Exception {

		// Allow some time for processing
		Thread.sleep(100);

		// Wait until all macro processing complete
		synchronized (this) {
			long startTime = System.currentTimeMillis();
			while (this.recordListener.macroTypes.size() > 0) {

				// Determine if time out
				long currentTime = System.currentTimeMillis();
				if ((currentTime - startTime) > 10 * 1000) {
					fail("Timed out waiting for macro adding to complete");
				}

				// Wait some time for completion
				Thread.sleep(100);
			}
		}

		// Wait test completed
		this.robot.waitForIdle();

		// Dispose of windows
		this.recordFrame.setVisible(false);
		this.recordFrame.dispose();
		this.mockFrame.setVisible(false);
		this.mockFrame.dispose();

		// Wait until disposed
		this.robot.waitForIdle();

		// Propagate possible macro failure
		if (this.macroFailure != null) {
			if (this.macroFailure instanceof Exception) {
				throw (Exception) this.macroFailure;
			} else {
				// Allow propagation
				throw new Exception(this.macroFailure);
			}
		}

		// Ensure thread safe verify
		synchronized (this.control(this.visibilityListener)) {
			// Verify functionality
			this.verifyMockObjects();
		}
	}

	/*
	 * ===================== Helper Methods ===============================
	 */

	/**
	 * Runs the input {@link Macro} on the {@link JComponent}.
	 * 
	 * @param macroFactory
	 *            {@link MacroSource} for the {@link Macro}.
	 * @param component
	 *            {@link JComponent}.
	 */
	private void runMacro(MacroSource macroFactory, JComponent component)
			throws Throwable {

		// Add macro factory
		final JMenuItem menuItem = this.recordComponent
				.addMacro(new MockMacroFactory(macroFactory));

		// Obtain location to right click for popup menu
		final Point macroLocation = getMouseLocation(component);

		// Right click on component for popup menu
		this.robot.mouseMove(macroLocation.x, macroLocation.y);
		this.robot.mousePress(InputEvent.BUTTON3_MASK);
		this.robot.mouseRelease(InputEvent.BUTTON3_MASK);
		this.robot.waitForIdle();

		// Move to macro menu item
		final Point menuItemLocation = getMouseLocation(menuItem);
		this.robot.mouseMove(menuItemLocation.x, menuItemLocation.y);

		// Click on the menu item
		this.robot.mousePress(InputEvent.BUTTON1_MASK);
		this.robot.mouseRelease(InputEvent.BUTTON1_MASK);
		this.robot.waitForIdle();

		// Allow processing of macro
		Thread.sleep(100);
		this.robot.waitForIdle();

		// Propagate possible macro failure
		if (this.macroFailure != null) {
			throw this.macroFailure;
		}
	}

	/**
	 * Obtains the relative mouse location to run a {@link Macro} on the input
	 * {@link JComponent}.
	 * 
	 * @param component
	 *            {@link JComponent}.
	 * @return Relative mouse location to run a {@link Macro} on the input
	 *         {@link JComponent}.
	 */
	private Point getRelativeMouseLocation(JComponent component) {

		// Obtain the absolute location
		Point absoluteLocation = getMouseLocation(component);

		// Return the relative location
		return this.getRelativeMouseLocation(absoluteLocation);
	}

	/**
	 * Obtains the relative location for the absolute location.
	 * 
	 * @param absoluteLocation
	 *            Absoluate location.
	 * @return Relative location.
	 */
	private Point getRelativeMouseLocation(Point absoluteLocation) {
		// Return relative location
		return new Point(absoluteLocation.x - this.offsetLocation.x,
				absoluteLocation.y - this.offsetLocation.y);
	}

	/**
	 * Obtains the location for the mouse to run a {@link Macro} on the input
	 * {@link JComponent}.
	 * 
	 * @param component
	 *            {@link JComponent}.
	 * @return Location for the mouse to run a {@link Macro} on the input
	 *         {@link JComponent}.
	 */
	private static Point getMouseLocation(JComponent component) {

		// Obtain the top left location of the component
		Point componentLocation = component.getLocationOnScreen();

		// Obtain location in middle of component
		Point mouseLocation = new Point(componentLocation.x
				+ (component.getWidth() / 2), componentLocation.y
				+ (component.getHeight() / 2));

		// Return the location
		return mouseLocation;
	}

	/**
	 * Mock backing window to validate recording.
	 */
	private class MockFrame extends JFrame {

		/**
		 * {@link JButton} that can be clicked.
		 */
		public final JButton button = new JButton("Click Me");

		/**
		 * Indicates if the button was clicked.
		 */
		public boolean isButtonClicked = false;

		/**
		 * Initiate.
		 */
		public MockFrame() {
			super("Mock Window");

			// Add the components for testing
			Container content = this.getContentPane();

			// Add Button
			content.add(this.button);
			this.button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					MockFrame.this.isButtonClicked = true;
				}
			});
		}
	}

	/**
	 * Mock {@link MacroSource} to report failures of {@link Macro}.
	 */
	private class MockMacroFactory implements MacroSource {

		/**
		 * Delegate {@link MacroSource}.
		 */
		private final MacroSource delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link MacroSource}.
		 */
		public MockMacroFactory(MacroSource delegate) {
			this.delegate = delegate;
		}

		/*
		 * ==================== MacroSource ===========================
		 */

		@Override
		public String getDisplayName() {
			return this.delegate.getDisplayName();
		}

		@Override
		public void sourceMacro(MacroSourceContext context) {
			this.delegate.sourceMacro(new MockMacroSourceContext(context));
		}
	}

	/**
	 * Mock {@link MacroSourceContext}.
	 */
	private class MockMacroSourceContext implements MacroSourceContext {

		/**
		 * Delegate.
		 */
		private final MacroSourceContext delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate.
		 */
		public MockMacroSourceContext(MacroSourceContext delegate) {
			this.delegate = delegate;
		}

		/*
		 * ====================== MacroSourceContext =========================
		 */

		@Override
		public Point getLocation() {
			return this.delegate.getLocation();
		}

		@Override
		public Point getAnotherLocation() {
			return this.delegate.getAnotherLocation();
		}

		@Override
		public Frame getOwnerFrame() {
			return this.delegate.getOwnerFrame();
		}

		@Override
		public Point getRelativeLocation(Point absoluteLocation) {
			return this.delegate.getRelativeLocation(absoluteLocation);
		}

		@Override
		public Point getAbsoluteLocation(Point relativeLocation) {
			return this.delegate.getAbsoluteLocation(relativeLocation);
		}

		@Override
		public void setNewMacro(Macro macro) {
			this.delegate.setNewMacro(new MockMacro(macro));
		}
	}

	/**
	 * Mock {@link Macro} to report failures.
	 */
	private class MockMacro implements Macro {

		/**
		 * Delegate {@link Macro}.
		 */
		private final Macro delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link Macro}.
		 */
		public MockMacro(Macro delegate) {
			this.delegate = delegate;
		}

		/*
		 * =================== Macro =====================================
		 */

		@Override
		public void setConfigurationMemento(String memento) {
			RecordComponentTest.this.macroFailure = new IllegalStateException(
					"Should not set configuration memento");
		}

		@Override
		public String getConfigurationMemento() {
			try {
				return this.delegate.getConfigurationMemento();
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
				return "";
			}
		}

		@Override
		public String getDisplayLabel() {
			try {
				return this.delegate.getDisplayLabel();
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
				return null;
			}
		}

		@Override
		public Point getStartingMouseLocation() {
			try {
				return this.delegate.getStartingMouseLocation();
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
				return null;
			}
		}

		@Override
		public MacroTask[] getMacroTasks() {
			MacroTask[] delegates = this.delegate.getMacroTasks();
			MacroTask[] tasks = new MacroTask[delegates.length];
			for (int i = 0; i < tasks.length; i++) {
				tasks[i] = new MockMacroTask(delegates[i]);
			}
			return tasks;
		}
	}

	/**
	 * Mock {@link MacroTask} to report failures.
	 */
	private class MockMacroTask implements MacroTask {

		/**
		 * Delegate {@link MacroTask}.
		 */
		private final MacroTask delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link MacroTask}.
		 */
		public MockMacroTask(MacroTask delegate) {
			this.delegate = delegate;
		}

		/*
		 * =================== MacroTask =============================
		 */

		@Override
		public void runMacroTask(MacroTaskContext context) {
			try {
				this.delegate.runMacroTask(context);
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
			}
		}

		@Override
		public long getPostRunWaitTime() {
			try {
				return this.delegate.getPostRunWaitTime();
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
				return 0;
			}
		}
	}

	/**
	 * Adds an expected {@link Macro}.
	 * 
	 * @param macroType
	 *            Expected {@link Macro} type.
	 * @param macroLocation
	 *            Expected {@link Macro} starting location.
	 * @param macroConfiguration
	 *            Expected {@link Macro} configuration.
	 */
	private synchronized void addExpectedMacro(
			Class<? extends Macro> macroType, Point macroLocation,
			String macroConfiguration) {
		this.recordListener.macroTypes.add(macroType);
		this.recordListener.macroLocations.add(macroLocation);
		this.recordListener.macroConfigs.add(macroConfiguration);
	}

	/**
	 * Mock {@link RecordListener}.
	 */
	private class MockRecordListener implements RecordListener {

		/**
		 * Listing of expected {@link Macro} classes.
		 */
		private final Deque<Class<? extends Macro>> macroTypes = new LinkedList<Class<? extends Macro>>();

		/**
		 * Listing of {@link Macro} locations.
		 */
		private final Deque<Point> macroLocations = new LinkedList<Point>();

		/**
		 * Listing of expected {@link Macro} configurations.
		 */
		private final Deque<String> macroConfigs = new LinkedList<String>();

		/*
		 * ================== RecordListener ============================
		 */

		@Override
		public void addMacro(Macro macro) {
			try {

				// Add the macro
				synchronized (RecordComponentTest.this) {
					// Obtain the macro
					macro = ((MockMacro) macro).delegate;

					// Ensure the expected macro type
					Class<? extends Macro> type = this.macroTypes.remove();
					assertEquals("Incorrect macro type", type, macro.getClass());

					// Ensure configuration is as expected
					String expectedMemento = this.macroConfigs.remove();
					String actualMemento = macro.getConfigurationMemento();
					assertEquals("Incorrect configuration", expectedMemento,
							actualMemento);

					// Obtain expected start location
					Point expectedStart = this.macroLocations.remove();

					// Obtain the actual start location
					Macro locationMacro = type.newInstance();
					locationMacro.setConfigurationMemento(macro
							.getConfigurationMemento());
					Point actualStart = locationMacro
							.getStartingMouseLocation();

					// Ensure same (given margin of error detecting location)
					assertTrue("Incorrect macro x configuration (e="
							+ expectedStart.x + ", a=" + actualStart.x + ")",
							(Math.abs(expectedStart.x - actualStart.x) <= 1));
					assertTrue("Incorrect macro y configuration (e="
							+ expectedStart.y + ", a=" + actualStart.y + ")",
							(Math.abs(expectedStart.y - actualStart.y) <= 1));
				}

			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
			}
		}
	}

}