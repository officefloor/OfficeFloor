/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
	 * Ensure not initialise if difference across commands for a parameter
	 * requires value.
	 */
	public void testConflictingParameterName() throws Exception {
		this.record_Factory("one", "option", null, true);
		this.record_Factory("two", "option", null, false);
		try {
			this.doTest("");
			fail("Should not parse");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Conflict in parameter 'option' requiring value",
					ex.getMessage());
		}
	}

	/**
	 * Ensure not initialise if difference across commands for a parameter (by
	 * short name) requires value.
	 */
	public void testConflictingParameterShortName() throws Exception {
		this.record_Factory("one", "optionA", "o", true);
		this.record_Factory("two", "optionB", "o", false);
		try {
			this.doTest("");
			fail("Should not parse");
		} catch (IllegalStateException ex) {
			assertEquals("Incorrect cause",
					"Conflict in parameter 'o' requiring value",
					ex.getMessage());
		}
	}

	/**
	 * Must have command for multiple commands.
	 */
	public void testNoCommand() throws Exception {
		this.record_Factory("command");
		try {
			this.doTest("");
			fail("Should not parse");
		} catch (OfficeFloorNoCommandsException ex) {
			assertEquals("Incorrect cause", "Must specify a command",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can parse single command.
	 */
	public void testSingleCommand() throws Exception {
		this.record_Factory("command");
		this.record_Command("command");
		this.doTest("command", "command");
	}

	/**
	 * Ensure can parse multiple commands.
	 */
	public void testMultipleCommands() throws Exception {
		this.record_Factory("a");
		this.record_Factory("b");
		this.record_Factory("c");
		this.record_Command("a");
		this.record_Command("b");
		this.record_Command("c");
		this.doTest("a b c", "a", "b", "c");
	}

	/**
	 * Ensure commands are returned in order of command line.
	 */
	public void testMultipleCommandsOrder() throws Exception {
		this.record_Factory("a");
		this.record_Factory("b");
		this.record_Factory("c");
		this.record_Command("a");
		this.record_Command("c");
		this.record_Command("b");
		this.doTest("a c b", "a", "c", "b");
	}

	/**
	 * Must have command even if flags provided.
	 */
	public void testFlagButNoCommand() throws Exception {
		this.record_Factory("command", "option", "o", false);
		try {
			this.doTest("-o");
			fail("Should not parse");
		} catch (OfficeFloorNoCommandsException ex) {
			assertEquals("Incorrect cause", "Must specify a command",
					ex.getMessage());
		}
	}

	/**
	 * Must have command even if options provided.
	 */
	public void testOptionButNoCommand() throws Exception {
		this.record_Factory("command", "option", "o", true);
		try {
			this.doTest("-o option");
			fail("Should not parse");
		} catch (OfficeFloorNoCommandsException ex) {
			assertEquals("Incorrect cause", "Must specify a command",
					ex.getMessage());
		}
	}

	/**
	 * Ensure can parse command with no parameter.
	 */
	public void testNoOption() throws Exception {
		this.record_Factory("command");
		this.record_Command("command");
		this.doTest("command", "command");
	}

	/**
	 * Ensure can parse command with descriptive flag.
	 */
	public void testDescriptiveFlag() throws Exception {
		this.record_Factory("command", "descriptive", null, false);
		this.record_Command("command", "descriptive", null);
		this.record_Argument("command", "descriptive", null);
		this.doTest("--descriptive command", "command");
	}

	/**
	 * Ensure can parse command with descriptive parameter.
	 */
	public void testDescriptiveOption() throws Exception {
		this.record_Factory("command", "descriptive", null, true);
		this.record_Command("command", "descriptive", null);
		this.record_Argument("command", "descriptive", "value");
		this.doTest("--descriptive value command", "command");
	}

	/**
	 * Ensure can parse command with repeated descriptive flag.
	 */
	public void testRepeatedDescriptiveFlag() throws Exception {
		this.record_Factory("command", "descriptive", null, false);
		this.record_Command("command", "descriptive", null);
		this.record_Argument("command", "descriptive", null);
		this.record_Argument("command", "descriptive", null);
		this.doTest("--descriptive --descriptive command", "command");
	}

	/**
	 * Ensure can parse command with repeated descriptive parameter.
	 */
	public void testRepeatedDescriptiveOption() throws Exception {
		this.record_Factory("command", "descriptive", null, true);
		this.record_Command("command", "descriptive", null);
		this.record_Argument("command", "descriptive", "A");
		this.record_Argument("command", "descriptive", "B");
		this.doTest("--descriptive A --descriptive B command", "command");
	}

	/**
	 * Ensure can parse command with short flag.
	 */
	public void testShortFlag() throws Exception {
		this.record_Factory("command", "short", "s", false);
		this.record_Command("command", "short", "s");
		this.record_Argument("command", "short", null);
		this.doTest("-s command", "command");
	}

	/**
	 * Ensure can parse command with short parameter.
	 */
	public void testShortOption() throws Exception {
		this.record_Factory("command", "short", "s", true);
		this.record_Command("command", "short", "s");
		this.record_Argument("command", "short", "value");
		this.doTest("-s value command", "command");
	}

	/**
	 * Ensure can parse command with repeated short flag.
	 */
	public void testRepeatedShortFlag() throws Exception {
		this.record_Factory("command", "short", "s", false);
		this.record_Command("command", "short", "s");
		this.record_Argument("command", "short", null);
		this.record_Argument("command", "short", null);
		this.doTest("-s -s command", "command");
	}

	/**
	 * Ensure can parse command with repeated short parameter.
	 */
	public void testRepeatedShortOption() throws Exception {
		this.record_Factory("command", "short", "s", true);
		this.record_Command("command", "short", "s");
		this.record_Argument("command", "short", "A");
		this.record_Argument("command", "short", "B");
		this.doTest("-s A -s B command", "command");
	}

	/**
	 * Ensure can load multiple flags.
	 */
	public void testMultipleFlags() throws Exception {
		this.record_Factory("command", "one", "1", false, "two", "2", false);
		this.record_Command("command", "one", "1", "two", "2");
		this.record_Argument("command", "one", null);
		this.record_Argument("command", "one", null);
		this.record_Argument("command", "two", null);
		this.record_Argument("command", "two", null);
		this.doTest("--one -1 --two -2 command", "command");
	}

	/**
	 * Ensure can load multiple options.
	 */
	public void testMultipleOptions() throws Exception {
		this.record_Factory("command", "one", "1", true, "two", "2", true);
		this.record_Command("command", "one", "1", "two", "2");
		this.record_Argument("command", "one", "a");
		this.record_Argument("command", "one", "b");
		this.record_Argument("command", "two", "c");
		this.record_Argument("command", "two", "d");
		this.doTest("--one a -1 b --two c -2 d command", "command");
	}

	/**
	 * Ensure can re-use flag across multiple commands.
	 */
	public void testReusedDescriptiveFlagAcrossCommands() throws Exception {
		this.record_Factory("one", "option", null, false);
		this.record_Factory("two", "option", null, false);
		this.record_Command("one", "option", null);
		this.record_Command("two", "option", null);
		this.record_Argument("one", "option", null);
		this.record_Argument("two", "option", null);
		this.doTest("--option one two", "one", "two");
	}

	/**
	 * Ensure can re-use parameter across multiple commands.
	 */
	public void testReusedDescriptiveOptionAcrossCommands() throws Exception {
		this.record_Factory("one", "option", null, true);
		this.record_Factory("two", "option", null, true);
		this.record_Command("one", "option", null);
		this.record_Command("two", "option", null);
		this.record_Argument("one", "option", "value");
		this.record_Argument("two", "option", "value");
		this.doTest("--option value one two", "one", "two");
	}

	/**
	 * Ensure can re-use flag across multiple commands.
	 */
	public void testReusedShortFlagAcrossCommands() throws Exception {
		this.record_Factory("one", "option", "o", false);
		this.record_Factory("two", "option", "o", false);
		this.record_Command("one", "option", "o");
		this.record_Command("two", "option", "o");
		this.record_Argument("one", "option", null);
		this.record_Argument("two", "option", null);
		this.doTest("-o one two", "one", "two");
	}

	/**
	 * Ensure can re-use parameter across multiple commands.
	 */
	public void testReusedShortOptionAcrossCommands() throws Exception {
		this.record_Factory("one", "option", "o", true);
		this.record_Factory("two", "option", "o", true);
		this.record_Command("one", "option", "o");
		this.record_Command("two", "option", "o");
		this.record_Argument("one", "option", "value");
		this.record_Argument("two", "option", "value");
		this.doTest("-o value one two", "one", "two");
	}

	/**
	 * Ensure can end options with a JVM option value for a system property. In
	 * other words a parameter value starting with '-' (e.g. <code>--jvm-option
	 * -Done=a</code>).
	 */
	public void testDescriptiveJvmOption() throws Exception {
		this.record_Factory("command", "jvm-option", null, true);
		this.record_Command("command", "jvm-option", null);
		this.record_Argument("command", "jvm-option", "-Done=a");
		this.doTest("--jvm-option -Done=a command", "command");
	}

	/**
	 * Ensure can end short options with a JVM option value for a system
	 * property.
	 */
	public void testShortJvmOption() throws Exception {
		this.record_Factory("command", "jvm-option", "jo", true);
		this.record_Command("command", "jvm-option", "jo");
		this.record_Argument("command", "jvm-option", "-Done=a");
		this.doTest("-jo -Done=a command", "command");
	}

	/**
	 * Ensure issue if flag followed by JVM option.
	 */
	public void testDescriptiveFlagFollowedByJvmOption() throws Exception {
		this.record_Factory("command", "flag", null, false);
		this.record_Command("command", "flag", null);
		try {
			this.doTest("--flag -Done=a command");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Unknown option -Done=a",
					ex.getMessage());
		}
	}

	/**
	 * Ensure issue if flag followed by JVM option.
	 */
	public void testShortFlagFollowedByJvmOption() throws Exception {
		this.record_Factory("command", "flag", "f", false);
		this.record_Command("command", "flag", null);
		try {
			this.doTest("-f -Done=a command");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Unknown option -Done=a",
					ex.getMessage());
		}
	}

}