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

package net.officefloor.demo.store;

import java.awt.Point;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import net.officefloor.demo.macro.LeftClickMacro;
import net.officefloor.demo.macro.Macro;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link MacroStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class MacroStoreTest extends OfficeFrameTestCase {

	/**
	 * {@link MacroStore} to test.
	 */
	private final MacroStore store = new MacroStore();

	/**
	 * Ensure able to store the {@link Macro} objects.
	 */
	public void testStore() throws IOException {

		// Create the example macros to store
		Macro leftClick = new LeftClickMacro();
		leftClick.setConfigurationMemento("1,1");
		Macro fullyQualified = new FullyQualifiedNameMacro();
		fullyQualified.setConfigurationMemento("test");

		// Store the macros
		StringWriter content = new StringWriter();
		this.store.store(new Macro[] { leftClick, fullyQualified }, content);

		// Create the expected macro stored content
		StringBuilder expected = new StringBuilder();
		expected.append(LeftClickMacro.class.getSimpleName() + ":1,1\n");
		expected.append(FullyQualifiedNameMacro.class.getName() + ":test\n");

		// Validate macro stored content
		assertEquals("Incorrect stored content", expected.toString(), content
				.toString());
	}

	/**
	 * Ensure able to retrieve the {@link Macro} objects.
	 */
	public void testRetrieve() throws Exception {

		// Create the store content
		StringBuilder content = new StringBuilder();
		content.append(LeftClickMacro.class.getSimpleName() + ":1,1\n");
		content.append(FullyQualifiedNameMacro.class.getName() + ":test\n");

		// Retrieve the macros
		Macro[] macros = this.store.retrieve(new StringReader(content
				.toString()));

		// Validate the retrieved macros
		assertEquals("Incorrect number of macros", 2, macros.length);
		Macro leftClick = macros[0];
		assertEquals("Incorrect macro type", LeftClickMacro.class, leftClick
				.getClass());
		assertEquals("Incorrect starting location", new Point(1, 1), leftClick
				.getStartingMouseLocation());
		Macro fullyQualified = macros[1];
		assertEquals("Incorrect macro type", FullyQualifiedNameMacro.class,
				fullyQualified.getClass());
		assertEquals("Incorrect configuration", "test", fullyQualified
				.getConfigurationMemento());
	}

	/**
	 * Ensure able to store and retrieve multi-line content.
	 */
	public void testStoreRetrieveMultilineContent() throws Exception {

		// Create the store buffer
		StringWriter content = new StringWriter();

		// Multi-line content
		String endOfLine = System.getProperty("line.separator");
		assertNotNull("Ensure have end of line", endOfLine);
		final String MULTI_LINE_CONTENT = "Multiple" + endOfLine + "line"
				+ endOfLine + "content" + endOfLine + "with \twhite spacing"
				+ endOfLine + " in content";

		// Store the multi-line content
		Macro macro = new FullyQualifiedNameMacro();
		macro.setConfigurationMemento(MULTI_LINE_CONTENT);
		this.store.store(new Macro[] { macro }, content);

		// Retrieve the macro with multi-line content
		Macro[] macros = this.store.retrieve(new StringReader(content
				.toString()));

		// Asset multi-line content
		assertEquals("Incorrect number of macros", 1, macros.length);
		Macro retrievedMacro = macros[0];
		assertTrue("Incorrect macro type",
				retrievedMacro instanceof FullyQualifiedNameMacro);
		FullyQualifiedNameMacro qualifiedMacro = (FullyQualifiedNameMacro) retrievedMacro;
		assertEquals("Incorrect multi-line content", MULTI_LINE_CONTENT,
				qualifiedMacro.getConfigurationMemento());
	}

}