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

package net.officefloor.demo.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;

import javax.swing.JFrame;

import net.officefloor.demo.record.RecordComponent;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link DemoTool}.
 * 
 * @author Daniel Sagenschneider
 */
public class DemoToolTest extends OfficeFrameTestCase {

	/**
	 * {@link JFrame} for the {@link DemoTool}.
	 */
	private JFrame frame;

	/*
	 * ===================== TestCase ====================
	 */

	@Override
	protected void setUp() throws Exception {
		// Create the frame
		this.frame = new JFrame();
		this.frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	@Override
	protected void tearDown() throws Exception {
		// Clean up the frame
		this.frame.setVisible(false);
		this.frame.dispose();
	}

	/**
	 * Ensure the correct recording area.
	 */
	@GuiTest
	public void testRecordingArea() throws Exception {

		// Create the frame with recording
		DemoTool demo = new DemoTool();
		demo.attachComponents(this.frame, null, this.frame);
		this.frame.setVisible(true);

		// Wait for frame to be visible
		Thread.sleep(100);
		new Robot().waitForIdle();

		// Obtain the recording area
		RecordComponent recordComponent = this.findRecordComponent(this.frame);
		assertNotNull("Can not find Record Component", recordComponent);

		// Obtain the record component location and size
		Point expectedLocation = recordComponent.getLocationOnScreen();
		Dimension expectedSize = recordComponent.getSize();

		// Obtain recording area
		Rectangle recordingArea = demo.getRecordingArea();

		// Ensure recording area is correct
		assertEquals("Incorrect x", expectedLocation.x, recordingArea.x);
		assertEquals("Incorrect y", expectedLocation.y, recordingArea.y);
		assertEquals("Incorrect width", expectedSize.width, recordingArea.width);
		assertEquals("Incorrect height", expectedSize.height,
				recordingArea.height);
	}

	/**
	 * Finds the {@link RecordComponent}.
	 * 
	 * @param component
	 *            Top level {@link Component}.
	 * @return {@link RecordComponent} or <code>null</code> if not found.
	 */
	private RecordComponent findRecordComponent(Component component) {

		// Determine if component the record component
		if (component instanceof RecordComponent) {
			return (RecordComponent) component;
		}

		// Search children for record component
		if (component instanceof Container) {
			Container container = (Container) component;
			for (Component child : container.getComponents()) {
				RecordComponent recordComponent = this
						.findRecordComponent(child);
				if (recordComponent != null) {
					// Found record component
					return recordComponent;
				}
			}
		}

		// Record component not found within component
		return null;
	}

}