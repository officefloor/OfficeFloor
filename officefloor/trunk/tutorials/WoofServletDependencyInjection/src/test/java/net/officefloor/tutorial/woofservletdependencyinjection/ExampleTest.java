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
package net.officefloor.tutorial.woofservletdependencyinjection;

import net.officefloor.tutorial.woofservletdependencyinjection.ExampleDependency;
import net.officefloor.tutorial.woofservletdependencyinjection.ExampleDependencyLocal;
import net.officefloor.tutorial.woofservletdependencyinjection.ExampleTemplateLogic;
import net.officefloor.tutorial.woofservletdependencyinjection.Message;
import junit.framework.TestCase;

/**
 * Tests the {@link ExampleTemplateLogic}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExampleTest extends TestCase {

	// START SNIPPET: test
	public void testTemplateLogic() {

		ExampleTemplateLogic logic = new ExampleTemplateLogic();

		// May mock interface to test but will use EJB for simplicity
		ExampleDependencyLocal ejb = new ExampleDependency();

		Message message = logic.getTemplate(ejb);
		assertEquals("Text for message", "MESSAGE", message.getText());
	}
	// END SNIPPET: test

}