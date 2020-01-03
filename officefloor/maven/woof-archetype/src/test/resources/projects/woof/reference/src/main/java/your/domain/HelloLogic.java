/*-
 * #%L
 * WoOF Archetype
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

package your.domain;

import lombok.Value;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.ObjectResponse;

public class HelloLogic {

	public void hello(
			@HttpPathParameter("name") String name, 
			ObjectResponse<Message> response) {
		response.send(new Message("Hello " + name));
	}

	@Value
	public static class Message {
		private String message;
	}
}
