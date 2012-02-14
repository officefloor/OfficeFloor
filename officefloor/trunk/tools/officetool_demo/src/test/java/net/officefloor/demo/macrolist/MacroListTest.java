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

package net.officefloor.demo.macrolist;

import org.easymock.AbstractMatcher;
import org.easymock.internal.AlwaysMatcher;

import net.officefloor.demo.macro.Macro;
import net.officefloor.demo.macrolist.MacroItem;
import net.officefloor.demo.macrolist.MacroList;
import net.officefloor.demo.macrolist.MacroListListener;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link MacroList}.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroListTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link MacroIndexFactory}.
	 */
	private final MacroIndexFactory indexFactory = this
			.createMock(MacroIndexFactory.class);

	/**
	 * Mock {@link MacroListListener}.
	 */
	private final MacroListListener listener = this
			.createMock(MacroListListener.class);

	/**
	 * {@link MacroList} to test.
	 */
	private final MacroList list = new MacroList(this.indexFactory,
			this.listener);

	/**
	 * Mock {@link Macro} for testing.
	 */
	private final Macro macro = this.createMock(Macro.class);

	/**
	 * Tests appending a {@link MacroList}.
	 */
	public void testAppendMacro() {

		// Record appending the macro (-1 created index)
		this.recordReturn(this.indexFactory, this.indexFactory
				.createMacroIndex(), -1);
		this.listener.macroAdded(null, 0);
		this.control(this.listener).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect macro", MacroListTest.this.macro,
						((MacroItem) actual[0]).getMacro());
				assertEquals("Incorrect index", expected[1], actual[1]);
				return true;
			}
		});

		// Add the macro
		this.replayMockObjects();
		this.list.addMacro(this.macro);
		this.verifyMockObjects();

		// Verify macro added to listing
		assertEquals("Incorrect number of macros", 1, this.list.size());
		MacroItem item = this.list.getItem(0);
		assertNotNull("Should have macro");
		assertEquals("Incorrect macro", this.macro, item.getMacro());
	}

	/**
	 * Tests adding a {@link MacroList}.
	 */
	public void testAddMacro() {

		// Record adding the macro
		this.recordReturn(this.indexFactory, this.indexFactory
				.createMacroIndex(), 0);
		this.listener.macroAdded(null, 0);
		this.control(this.listener).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect macro", MacroListTest.this.macro,
						((MacroItem) actual[0]).getMacro());
				assertEquals("Incorrect index", expected[1], actual[1]);
				return true;
			}
		});

		// Add the macro
		this.replayMockObjects();
		this.list.addMacro(this.macro);
		this.verifyMockObjects();

		// Verify macro added to listing
		assertEquals("Incorrect number of macros", 1, this.list.size());
		MacroItem item = this.list.getItem(0);
		assertNotNull("Should have macro");
		assertEquals("Incorrect macro", this.macro, item.getMacro());
	}

	/**
	 * Tests removing a {@link MacroItem}.
	 */
	public void testRemoveMacro() {

		// Record adding then removing the macro
		this.recordReturn(this.indexFactory, this.indexFactory
				.createMacroIndex(), -1);
		this.listener.macroAdded(null, 0);
		this.control(this.listener).setMatcher(new AlwaysMatcher());
		this.listener.macroRemoved(null, 0);
		this.control(this.listener).setMatcher(new AbstractMatcher() {
			@Override
			public boolean matches(Object[] expected, Object[] actual) {
				assertEquals("Incorrect macro", MacroListTest.this.macro,
						((MacroItem) actual[0]).getMacro());
				assertEquals("Incorrect index", expected[1], actual[1]);
				return true;
			}
		});

		this.replayMockObjects();

		// Add the macro
		this.list.addMacro(this.macro);

		assertEquals("Ensure added", this.macro, this.list.getItem(0)
				.getMacro());

		// Remove the macro
		this.list.removeItem(0);

		this.verifyMockObjects();

		// Verify macro removed
		assertEquals("Incorrect size", 0, this.list.size());
	}

}