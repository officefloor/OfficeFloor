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
package net.officefloor.demo.record;

import java.awt.Container;
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

import junit.framework.TestCase;
import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macro.MacroContext;
import net.officefloor.demo.macro.MacroFactory;
import net.officefloor.demo.record.RecordComponent;
import net.officefloor.demo.record.RecordListener;

/**
 * Test the {@link RecordComponent}.
 * 
 * @author Daniel Sagenschneider
 */
public class RecordComponentTest extends TestCase {

	/**
	 * {@link RecordComponent} being tested.
	 */
	private RecordComponent recordComponent;

	/**
	 * Mock {@link RecordListener}.
	 */
	private final MockRecordListener recordListener = new MockRecordListener();

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
	private Throwable macroFailure = null;

	/**
	 * {@link Robot} to aid in unit testing.
	 */
	private Robot robot;

	/**
	 * Tests single click.
	 */
	public void testSingleClick() throws Throwable {
		final Point location = this
				.getRelativeMouseLocation(this.mockFrame.button);
		this.addExpectedMacro(LeftClickMacro.class, location.x + ","
				+ location.y);
		this.runMacro(new LeftClickMacro(), this.mockFrame.button);
		assertTrue("Should click button", this.mockFrame.isButtonClicked);
	}

	/*
	 * ===================== TestCase ==================================
	 */

	@Override
	protected void setUp() throws Exception {

		// Create Robot to aid in testing
		this.robot = new Robot();
		this.robot.setAutoDelay(100); // allow invoking

		// Create the mock window first to be in background
		this.mockFrame = new MockFrame();
		this.mockFrame.setLocation(100, 100);
		this.mockFrame.setSize(400, 400);
		this.mockFrame.setVisible(true);
		this.robot.waitForIdle(); // Wait for window to appear

		// Create window containing the record component in front
		this.recordFrame = new JFrame();
		this.recordComponent = new RecordComponent(new Robot(),
				this.recordFrame, this.recordListener);
		this.recordFrame.setTitle("Recorder");
		this.recordFrame.setLocation(100, 100);
		this.recordFrame.setSize(400, 400);
		this.recordFrame.getContentPane().add(this.recordComponent);
		this.recordFrame.setVisible(true);
		this.robot.waitForIdle(); // Wait for window to appear

		// Bring record to the front
		this.recordFrame.setVisible(true);
	}

	@Override
	protected void tearDown() throws Exception {

		// Wait test completed
		this.robot.waitForIdle();

		// Dispose of windows
		this.recordFrame.setVisible(false);
		this.recordFrame.dispose();
		this.mockFrame.setVisible(false);
		this.mockFrame.dispose();

		// Wait until disposed
		this.robot.waitForIdle();

		// Ensure correctly recorded
		assertEquals("Macro not recorded", 0, this.recordListener.macroTypes
				.size());
	}

	/*
	 * ===================== Helper Methods ===============================
	 */

	/**
	 * Runs the input {@link Macro} on the {@link JComponent}.
	 * 
	 * @param macroFactory
	 *            {@link MacroFactory} for the {@link Macro}.
	 * @param component
	 *            {@link JComponent}.
	 */
	private void runMacro(MacroFactory macroFactory, JComponent component)
			throws Throwable {

		// Add macro factory
		final JMenuItem menuItem = this.recordComponent
				.addMacro(new MockMacroFactory(macroFactory));

		// Obtain location to right click for popup menu
		final Point macroLocation = getMouseLocation(component);

		// Right click on component for popup menu
		RecordComponentTest.this.robot.mouseMove(macroLocation.x,
				macroLocation.y);
		RecordComponentTest.this.robot.mousePress(InputEvent.BUTTON3_MASK);
		RecordComponentTest.this.robot.mouseRelease(InputEvent.BUTTON3_MASK);
		RecordComponentTest.this.robot.waitForIdle();

		// Move to macro menu item
		final Point menuItemLocation = getMouseLocation(menuItem);
		RecordComponentTest.this.robot.mouseMove(menuItemLocation.x,
				menuItemLocation.y);

		// Click on the menu item
		RecordComponentTest.this.robot.mousePress(InputEvent.BUTTON1_MASK);
		RecordComponentTest.this.robot.mouseRelease(InputEvent.BUTTON1_MASK);
		RecordComponentTest.this.robot.waitForIdle();

		// Allow processing of macro
		Thread.sleep(100);

		// Wait until processing of macro idle
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

		// Return relative location
		Point frameLocation = this.recordComponent.getLocationOnScreen();
		return new Point(absoluteLocation.x - frameLocation.x,
				absoluteLocation.y - frameLocation.y);
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
	 * Mock {@link MacroFactory} to report failures of {@link Macro}.
	 */
	private class MockMacroFactory implements MacroFactory {

		/**
		 * Delegate {@link MacroFactory}.
		 */
		private final MacroFactory delegate;

		/**
		 * Initiate.
		 * 
		 * @param delegate
		 *            Delegate {@link MacroFactory}.
		 */
		public MockMacroFactory(MacroFactory delegate) {
			this.delegate = delegate;
		}

		/*
		 * ==================== MacroFactory ===========================
		 */

		@Override
		public String getDisplayName() {
			return this.delegate.getDisplayName();
		}

		@Override
		public Macro createMacro(Point location) {
			return new MockMacro(this.delegate.createMacro(location));
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
		public Point getStartingMouseLocation() {
			try {
				return this.delegate.getStartingMouseLocation();
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
				return null;
			}
		}

		@Override
		public void runMacro(MacroContext context) {
			try {
				this.delegate.runMacro(context);
			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
			}
		}
	}

	/**
	 * Adds an expected {@link Macro}.
	 * 
	 * @param macroType
	 *            Expected {@link Macro} type.
	 * @param macroConfiguration
	 *            Expected {@link Macro} configuration.
	 */
	private void addExpectedMacro(Class<? extends Macro> macroType,
			String macroConfiguration) {
		this.recordListener.macroTypes.add(macroType);
		this.recordListener.macroConfigs.add(macroConfiguration);
	}

	/**
	 * Mock {@link RecordListener}.
	 */
	private class MockRecordListener implements RecordListener {

		/**
		 * Listing of expected {@link Macro} classes.
		 */
		private final Deque<Class<?>> macroTypes = new LinkedList<Class<?>>();

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
				// Obtain the macro
				macro = ((MockMacro) macro).delegate;

				// Ensure the expected macro type
				Class<?> type = this.macroTypes.remove();
				assertEquals("Incorrect macro type", type, macro.getClass());

				// Obtain expected start location
				String configuration = this.macroConfigs.remove();
				LeftClickMacro leftClick = new LeftClickMacro();
				leftClick.setConfigurationMemento(configuration);
				Point expectedStart = leftClick.getStartingMouseLocation();

				// Obtain the actual start location
				configuration = macro.getConfigurationMemento();
				leftClick.setConfigurationMemento(configuration);
				Point actualStart = leftClick.getStartingMouseLocation();

				// Ensure same (given margin of error detecting location)
				assertTrue("Incorrect macro x configuration (e="
						+ expectedStart.x + ", a=" + actualStart.x + ")", (Math
						.abs(expectedStart.x - actualStart.x) < 10));
				assertTrue("Incorrect macro y configuration (e="
						+ expectedStart.y + ", a=" + actualStart.y + ")", (Math
						.abs(expectedStart.y - actualStart.y) < 10));

			} catch (Throwable ex) {
				RecordComponentTest.this.macroFailure = ex;
			}
		}
	}

}