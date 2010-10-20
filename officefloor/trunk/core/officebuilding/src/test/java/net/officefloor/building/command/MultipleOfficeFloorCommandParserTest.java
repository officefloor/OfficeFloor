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
 * Tests the {@link OfficeFloorCommandParser} providing multiple
 * {@link OfficeFloorCommand} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class MultipleOfficeFloorCommandParserTest extends
		AbstractOficeFloorCommandParserTestCase {

	/**
	 * Must have command for multiple commands.
	 */
	public void testNoCommand() {
		this.record_Factories("command");
		try {
			this.doTest("");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Must specify a command", ex
					.getMessage());
		}
	}

	/**
	 * Ensure can parse single command.
	 */
	public void testSingleCommand() throws Exception {
		this.record_Factories("command");
		this.record_Command("command");
		this.doTest("command", "command");
	}

	/**
	 * Ensure can parse multiple commands.
	 */
	public void testMultipleCommands() throws Exception {
		this.record_Factories("a", "b", "c");
		this.record_Command("a");
		this.record_Command("b");
		this.record_Command("c");
		this.doTest("a b c", "a", "b", "c");
	}

	/**
	 * Ensure commands are returned in order of command line.
	 */
	public void testMultipleCommandsOrder() throws Exception {
		this.record_Factories("a", "b", "c");
		this.record_Command("a");
		this.record_Command("c");
		this.record_Command("b");
		this.doTest("a c b", "a", "c", "b");
	}

	/**
	 * Must have command even if options provided.
	 */
	public void testOptionButNoCommand() {
		this.record_Factories("command");
		try {
			this.doTest("-o option");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Must specify a command", ex
					.getMessage());
		}
	}

	/**
	 * Ensure can parse command with no parameter.
	 */
	public void testNoOption() throws Exception {
		this.record_Factories("command");
		this.record_Command("command");
		this.doTest("command", "command");
	}

	/**
	 * Ensure can parse command with descriptive parameter.
	 */
	public void testDescriptiveOption() throws Exception {
		this.record_Factories("command");
		this.record_Command("command", "descriptive", null);
		this.record_Argument("command", "descriptive", "value");
		this.doTest("--descriptive value command", "command");
	}

	/**
	 * Ensure can parse command with repeated descriptive parameter.
	 */
	public void testRepeatedDescriptiveOption() throws Exception {
		this.record_Factories("command");
		this.record_Command("command", "descriptive", null);
		this.record_Argument("command", "descriptive", "A");
		this.record_Argument("command", "descriptive", "B");
		this.doTest("--descriptive A --descriptive B command", "command");
	}

	/**
	 * Ensure can parse command with short parameter.
	 */
	public void testShortOption() throws Exception {
		this.record_Factories("command");
		this.record_Command("command", "short", "s");
		this.record_Argument("command", "short", "value");
		this.doTest("-s value command", "command");
	}

	/**
	 * Ensure can parse command with repeated short parameter.
	 */
	public void testRepeatedShortOption() throws Exception {
		this.record_Factories("command");
		this.record_Command("command", "short", "s");
		this.record_Argument("command", "short", "A");
		this.record_Argument("command", "short", "B");
		this.doTest("-s A -s B command", "command");
	}

	/**
	 * Ensure can load multiple options.
	 */
	public void testMultipleOptions() throws Exception {
		this.record_Factories("command");
		this.record_Command("command", "one", "1", "two", "2");
		this.record_Argument("command", "one", "a");
		this.record_Argument("command", "one", "b");
		this.record_Argument("command", "two", "c");
		this.record_Argument("command", "two", "d");
		this.doTest("--one a -1 b --two c -2 d command", "command");
	}

	/**
	 * Ensure can re-use parameter across multiple commands.
	 */
	public void testReusedDescriptiveOptionAcrossCommands() throws Exception {
		this.record_Factories("one", "two");
		this.record_Command("one", "option", null);
		this.record_Command("two", "option", null);
		this.record_Argument("one", "option", "value");
		this.record_Argument("two", "option", "value");
		this.doTest("--option value one two", "one", "two");
	}

	/**
	 * Ensure can re-use parameter across multiple commands.
	 */
	public void testReusedShortOptionAcrossCommands() throws Exception {
		this.record_Factories("one", "two");
		this.record_Command("one", "option", "o");
		this.record_Command("two", "option", "o");
		this.record_Argument("one", "option", "value");
		this.record_Argument("two", "option", "value");
		this.doTest("-o value one two", "one", "two");
	}

}