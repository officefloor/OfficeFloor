/*-
 * #%L
 * JDBC Persistence
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

package net.officefloor.jdbc.decorate;

import java.sql.Connection;

/**
 * Decorator on all created {@link Connection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ConnectionDecorator {

	/**
	 * Allows decorating the {@link Connection}.
	 * 
	 * @param connection {@link Connection} to decorate.
	 * @return Decorated {@link Connection} or possibly new wrapping
	 *         {@link Connection} implementation.
	 */
	Connection decorate(Connection connection);

}
