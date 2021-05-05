/*-
 * #%L
 * [bundle] OfficeFloor Editor
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

package net.officefloor.gef.editor;

import net.officefloor.model.ConnectionModel;

/**
 * Adapted {@link ConnectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnection<C extends ConnectionModel> extends AdaptedModel<C> {

	/**
	 * Obtains the source {@link AdaptedConnectable}.
	 * 
	 * @return Source {@link AdaptedConnectable}.
	 */
	AdaptedConnectable<?> getSource();

	/**
	 * Obtains the target {@link AdaptedConnectable}.
	 * 
	 * @return Target {@link AdaptedConnectable}.
	 */
	AdaptedConnectable<?> getTarget();

	/**
	 * Indicates whether able to remove the {@link ConnectionModel}.
	 * 
	 * @return <code>true</code> if able to remove the {@link ConnectionModel}.
	 */
	boolean canRemove();

	/**
	 * Removes the {@link ConnectionModel}.
	 */
	void remove();

}
