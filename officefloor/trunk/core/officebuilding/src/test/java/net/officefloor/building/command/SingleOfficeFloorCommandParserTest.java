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
package net.officefloor.building.command;

/**
 * Tests the {@link OfficeFloorCommandParser} providing single
 * {@link OfficeFloorCommand} instance.
 * 
 * @author Daniel Sagenschneider
 */
public class SingleOfficeFloorCommandParserTest extends
		AbstractOficeFloorCommandParserTestCase {

	/**
	 * Name of the command.
	 */
	private static final String COMMAND = "command";

	@Override
	protected void setUp() throws Exception {
		this.flagSingle();
		this.record_Factories(COMMAND);
	}

	/**
	 * Ensure no issue if no options nor commands.
	 */
	public void testEmptyCommandLine() throws Exception {
		this.record_Command(COMMAND);
		this.doTest("", COMMAND);
	}

	/**
	 * Ensure issue if single command.
	 */
	public void testSingleCommand() throws Exception {
		this.record_Command(COMMAND);
		try {
			this.doTest("command");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Must not provide command", ex
					.getMessage());
		}
	}

	/**
	 * Ensure issue if multiple commands.
	 */
	public void testMultipleCommands() throws Exception {
		this.record_Command(COMMAND);
		try {
			this.doTest("a b c");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Must not provide command", ex
					.getMessage());
		}
	}

	/**
	 * Ensure can parse command with descriptive parameter.
	 */
	public void testDescriptiveOption() throws Exception {
		this.record_Command(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", "value");
		this.doTest("--descriptive value", COMMAND);
	}

	/**
	 * Ensure can parse command with repeated descriptive parameter.
	 */
	public void testRepeatedDescriptiveOption() throws Exception {
		this.record_Command(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", "A");
		this.record_Argument(COMMAND, "descriptive", "B");
		this.doTest("--descriptive A --descriptive B", COMMAND);
	}

	/**
	 * Ensure can parse command with short parameter.
	 */
	public void testShortOption() throws Exception {
		this.record_Command(COMMAND, "short", "s");
		this.record_Argument(COMMAND, "short", "value");
		this.doTest("-s value", COMMAND);
	}

	/**
	 * Ensure can parse command with repeated short parameter.
	 */
	public void testRepeatedShortOption() throws Exception {
		this.record_Command(COMMAND, "short", "s");
		this.record_Argument(COMMAND, "short", "A");
		this.record_Argument(COMMAND, "short", "B");
		this.doTest("-s A -s B", COMMAND);
	}

	/**
	 * Ensure can load multiple options.
	 */
	public void testMultipleOptions() throws Exception {
		this.record_Command(COMMAND, "one", "1", "two", "2");
		this.record_Argument(COMMAND, "one", "a");
		this.record_Argument(COMMAND, "one", "b");
		this.record_Argument(COMMAND, "two", "c");
		this.record_Argument(COMMAND, "two", "d");
		this.doTest("--one a -1 b --two c -2 d", COMMAND);
	}

}