/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.variable;

import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextFunction;

/**
 * Test using the {@link Var}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableTest extends OfficeFrameTestCase {

	/**
	 * Ensure can {@link Out} then {@link In}.
	 */
	public void testOutIn() throws Exception {

		// Compile section
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.office((context) -> context.addSection("SECTION", OutInSection.class));
		OfficeFloor officeFloor = compiler.compileAndOpenOfficeFloor();

	}

	public static class OutInSection {

		@NextFunction("stepTwo")
		public void stepOne(Out<String> text, Out<Integer> number) {
			text.set("TEXT");
			number.set(1);
		}

		public void stepTwo(In<String> text, In<Integer> number) {
			assertEquals("Incorrect text", "TEXT", text.get());
			assertEquals("Incorrect number", Integer.valueOf(1), number.get());
		}
	}

	public static class OutValSection {

		@NextFunction("stepTwo")
		public void stepOne(Out<String> text, Out<Integer> number) {
			text.set("TEXT");
			number.set(1);
		}

		public void stepTwo(@Val String text, @Val Integer number) {
			assertEquals("Incorrect text", "TEXT", text);
			assertEquals("Incorrect number", Integer.valueOf(1), number);
		}
	}

}