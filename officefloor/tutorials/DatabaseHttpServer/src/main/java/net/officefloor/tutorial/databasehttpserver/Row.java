/*-
 * #%L
 * Database HTTP Server Tutorial
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

package net.officefloor.tutorial.databasehttpserver;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpParameters;

/**
 * Represents a row from the table in the database.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: example
@Data
@AllArgsConstructor
@NoArgsConstructor
@HttpParameters
public class Row implements Serializable {
	private static final long serialVersionUID = 1L;

	private int id;

	private String name;

	private String description;
}
// END SNIPPET: example
