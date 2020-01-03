/*-
 * #%L
 * Variable Tutorial
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.tutorial.variablehttpserver;

import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.web.ObjectResponse;

/**
 * Using {@link Out} and {@link In} for variables.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class InLogic {

	public void useValues(In<Person> person, @Description In<String> description,
			ObjectResponse<ServerResponse> response) {
		response.send(new ServerResponse(person.get(), description.get()));
	}
}
// END SNIPPET: tutorial
