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
import java.io.StringReader;
import java.io.StringWriter;

import net.officefloor.demo.store.MacroStore;
import junit.framework.TestCase;

/**
 * Tests storing and retrieving the {@link Macro} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroStoreRetrieveTest extends TestCase {

	/**
	 * Tests the {@link CommentMacro}.
	 */
	public void testCommentMacro() {
		CommentMacro macro = this.storeRetrieveMacro(CommentMacro.class,
				"comment");
		assertEquals("Incorrect comment", "comment", macro.getComment());
	}

	/**
	 * Tests the {@link LeftClickMacro}.
	 */
	public void testLeftClickMacro() {
		LeftClickMacro macro = this.storeRetrieveMacro(LeftClickMacro.class,
				"10,20");
		assertEquals("Incorrect starting location", new Point(10, 20), macro
				.getStartingMouseLocation());
	}

	/**
	 * Tests the {@link RightClickMacro}.
	 */
	public void testRightClickMacro() {
		RightClickMacro macro = this.storeRetrieveMacro(RightClickMacro.class,
				"10,20");
		assertEquals("Incorrect starting location", new Point(10, 20), macro
				.getStartingMouseLocation());
	}

	/**
	 * Tests the {@link InputTextMacro}.
	 */
	public void testInputTextMacro() {
		InputTextMacro macro = this.storeRetrieveMacro(InputTextMacro.class,
				"text");
		assertEquals("Incorrect input text", "text", macro.getInputText());
	}

	/**
	 * Tests the {@link DragMacro}.
	 */
	public void testDragMacro() {
		DragMacro macro = this.storeRetrieveMacro(DragMacro.class, "1,2-10,20");
		assertEquals("Incorrect starting location", new Point(1, 2), macro
				.getStartingMouseLocation());
		assertEquals("Incorrect target location", new Point(10, 20), macro
				.getTargetLocation());
	}

	/**
	 * Tests the {@link InfoMacro}.
	 */
	public void testInfoMacro() {
		InfoMacro macro = this
				.storeRetrieveMacro(InfoMacro.class, "10,20:test");
		assertEquals("Incorrect location", new Point(10, 20), macro
				.getInfoLocation());
		assertEquals("Incorrect text", "test", macro.getInfoText());
	}

	/**
	 * Tests the {@link MoveMouseMacro}.
	 */
	public void testMoveMouseMacro() {
		MoveMouseMacro macro = this.storeRetrieveMacro(MoveMouseMacro.class,
				"10,20");
		assertEquals("Incorrect starting location", new Point(10, 20), macro
				.getStartingMouseLocation());
	}

	/**
	 * Tests the {@link PauseMacro}.
	 */
	public void testPauseMacro() {
		PauseMacro macro = this.storeRetrieveMacro(PauseMacro.class, "2000");
		assertEquals("Incorrect pause time", 2000, macro.getPauseTime());
	}

	/**
	 * Stores the {@link Macro} and returns the retrieved {@link Macro}.
	 * 
	 * @param macro
	 *            {@link Macro} to store.
	 * @param configurationMemento
	 *            Configuration memento.
	 * @return Retrieved {@link Macro}.
	 */
	@SuppressWarnings("unchecked")
	private <M extends Macro> M storeRetrieveMacro(Class<M> macroType,
			String configurationMemento) {
		try {
			// Create store and content
			MacroStore store = new MacroStore();
			StringWriter content = new StringWriter();

			// Create and configure the macro
			M macro = macroType.newInstance();
			macro.setConfigurationMemento(configurationMemento);

			// Store the macro
			store.store(new Macro[] { macro }, content);

			// Validate the content
			String expectedContent = macro.getClass().getSimpleName() + ":"
					+ configurationMemento + "\n";
			String actualContent = content.toString();
			assertEquals("Incorrect content", expectedContent, actualContent);

			// Retrieve the macro
			Macro[] macros = store
					.retrieve(new StringReader(content.toString()));
			assertEquals("Should only be a single macro", 1, macros.length);

			// Return the typed macro
			return (M) macros[0];

		} catch (Exception ex) {
			fail("Should not fail storing and retrieving: " + ex.getMessage()
					+ " [" + ex.getClass().getSimpleName() + "]");
			return null;
		}
	}
}