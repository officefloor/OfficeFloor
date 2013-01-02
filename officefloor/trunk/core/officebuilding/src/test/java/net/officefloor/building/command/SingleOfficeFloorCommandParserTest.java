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
	}

	/**
	 * Ensure no issue if no options nor commands.
	 */
	public void testEmptyCommandLine() throws Exception {
		this.record_Factory(COMMAND);
		this.record_Command(COMMAND);
		this.doTest("", COMMAND);
	}

	/**
	 * Ensure issue if single command.
	 */
	public void testSingleCommand() throws Exception {
		this.record_Factory(COMMAND);
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
		this.record_Factory(COMMAND);
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
	 * Ensure can parse command with descriptive flag.
	 */
	public void testDescriptiveFlag() throws Exception {
		this.record_Factory(COMMAND, "descriptive", null, false);
		this.record_Command(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", null);
		this.doTest("--descriptive", COMMAND);
	}

	/**
	 * Ensure can parse command with descriptive parameter.
	 */
	public void testDescriptiveOption() throws Exception {
		this.record_Factory(COMMAND, "descriptive", null, true);
		this.record_Command(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", "value");
		this.doTest("--descriptive value", COMMAND);
	}

	/**
	 * Ensure can parse command with repeated descriptive flag.
	 */
	public void testRepeatedDescriptiveFlag() throws Exception {
		this.record_Factory(COMMAND, "descriptive", null, false);
		this.record_Command(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", null);
		this.doTest("--descriptive --descriptive", COMMAND);
	}

	/**
	 * Ensure can parse command with repeated descriptive parameter.
	 */
	public void testRepeatedDescriptiveOption() throws Exception {
		this.record_Factory(COMMAND, "descriptive", null, true);
		this.record_Command(COMMAND, "descriptive", null);
		this.record_Argument(COMMAND, "descriptive", "A");
		this.record_Argument(COMMAND, "descriptive", "B");
		this.doTest("--descriptive A --descriptive B", COMMAND);
	}

	/**
	 * Ensure can parse command with short flag.
	 */
	public void testShortFlag() throws Exception {
		this.record_Factory(COMMAND, "short", "s", false);
		this.record_Command(COMMAND, "short", "s");
		this.record_Argument(COMMAND, "short", null);
		this.doTest("-s", COMMAND);
	}

	/**
	 * Ensure can parse command with short parameter.
	 */
	public void testShortOption() throws Exception {
		this.record_Factory(COMMAND, "short", "s", true);
		this.record_Command(COMMAND, "short", "s");
		this.record_Argument(COMMAND, "short", "value");
		this.doTest("-s value", COMMAND);
	}

	/**
	 * Ensure can parse command with repeated short flag.
	 */
	public void testRepeatedShortFlag() throws Exception {
		this.record_Factory(COMMAND, "short", "s", false);
		this.record_Command(COMMAND, "short", "s");
		this.record_Argument(COMMAND, "short", null);
		this.record_Argument(COMMAND, "short", null);
		this.doTest("-s -s", COMMAND);
	}

	/**
	 * Ensure can parse command with repeated short parameter.
	 */
	public void testRepeatedShortOption() throws Exception {
		this.record_Factory(COMMAND, "short", "s", true);
		this.record_Command(COMMAND, "short", "s");
		this.record_Argument(COMMAND, "short", "A");
		this.record_Argument(COMMAND, "short", "B");
		this.doTest("-s A -s B", COMMAND);
	}

	/**
	 * Ensure can load multiple flags.
	 */
	public void testMultipleFlags() throws Exception {
		this.record_Factory(COMMAND, "one", "1", false, "two", "2", false);
		this.record_Command(COMMAND, "one", "1", "two", "2");
		this.record_Argument(COMMAND, "one", null);
		this.record_Argument(COMMAND, "one", null);
		this.record_Argument(COMMAND, "two", null);
		this.record_Argument(COMMAND, "two", null);
		this.doTest("--one -1 --two -2", COMMAND);
	}

	/**
	 * Ensure can load multiple options.
	 */
	public void testMultipleOptions() throws Exception {
		this.record_Factory(COMMAND, "one", "1", true, "two", "2", true);
		this.record_Command(COMMAND, "one", "1", "two", "2");
		this.record_Argument(COMMAND, "one", "a");
		this.record_Argument(COMMAND, "one", "b");
		this.record_Argument(COMMAND, "two", "c");
		this.record_Argument(COMMAND, "two", "d");
		this.doTest("--one a -1 b --two c -2 d", COMMAND);
	}

	/**
	 * Ensure can end options with a JVM option value for a system property. In
	 * other words a parameter value starting with '-' (e.g. <code>--jvm-option
	 * -Done=a</code>).
	 */
	public void testDescriptiveJvmOption() throws Exception {
		this.record_Factory(COMMAND, "jvm-option", null, true);
		this.record_Command(COMMAND, "jvm-option", null);
		this.record_Argument(COMMAND, "jvm-option", "-Done=a");
		this.doTest("--jvm-option -Done=a", COMMAND);
	}

	/**
	 * Ensure can end short options with a JVM option value for a system
	 * property.
	 */
	public void testShortJvmOption() throws Exception {
		this.record_Factory(COMMAND, "jvm-option", "jo", true);
		this.record_Command(COMMAND, "jvm-option", "jo");
		this.record_Argument(COMMAND, "jvm-option", "-Done=a");
		this.doTest("-jo -Done=a", COMMAND);
	}

	/**
	 * Ensure issue if flag followed by JVM option.
	 */
	public void testDescriptiveFlagFollowedByJvmOption() throws Exception {
		this.record_Factory(COMMAND, "flag", null, false);
		this.record_Command(COMMAND, "flag", null);
		try {
			this.doTest("--flag -Done=a");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Unknown option -Done=a", ex
					.getMessage());
		}
	}

	/**
	 * Ensure issue if flag followed by JVM option.
	 */
	public void testShortFlagFollowedByJvmOption() throws Exception {
		this.record_Factory(COMMAND, "flag", "f", false);
		this.record_Command(COMMAND, "flag", null);
		try {
			this.doTest("-f -Done=a");
			fail("Should not parse");
		} catch (OfficeFloorCommandParseException ex) {
			assertEquals("Incorrect cause", "Unknown option -Done=a", ex
					.getMessage());
		}
	}

}