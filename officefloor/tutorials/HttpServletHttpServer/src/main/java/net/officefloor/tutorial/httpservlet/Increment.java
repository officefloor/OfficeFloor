/*-
 * #%L
 * Embedding WoOF within HttpServlet container
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

package net.officefloor.tutorial.httpservlet;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.web.HttpObject;
import net.officefloor.web.ObjectResponse;

/**
 * Section {@link ClassSectionSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class Increment {

	@Data
	@HttpObject
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Request {
		private String value;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Response {
		private String value;
	}

	public void increment(Request request, ObjectResponse<Response> response) {
		int value = Integer.parseInt(request.getValue());
		response.send(new Response(String.valueOf(value + 1)));
	}

}
