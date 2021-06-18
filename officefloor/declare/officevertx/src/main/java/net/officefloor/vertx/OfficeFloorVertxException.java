/*-
 * #%L
 * Vertx
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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

package net.officefloor.vertx;

import io.vertx.core.Vertx;

/**
 * Wrapper {@link Exception} for {@link Throwable} from {@link Vertx}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorVertxException extends Exception {

	/**
	 * Serialisaion verstion.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instantiate.
	 * 
	 * @param cause Cause.
	 */
	public OfficeFloorVertxException(Throwable cause) {
		super(cause);
	}

}
