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
package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;

/**
 * Using {@link Out} and {@link In} for variables.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class OutLogic {

	@NextFunction("use")
	public void setValues(Out<Person> person, @Description Out<String> description) {
		person.set(new Person("Daniel", "Sagenschneider"));
		description.set("Need to watch his code!");
	}
}
// END SNIPPET: tutorial