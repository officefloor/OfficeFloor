/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor;

import net.officefloor.model.ConnectionModel;

/**
 * Adapted {@link ConnectionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnection<C extends ConnectionModel> extends AdaptedModel<C> {

	/**
	 * Obtains the source {@link AdaptedChild}.
	 * 
	 * @return Source {@link AdaptedChild}.
	 */
	AdaptedChild<?> getSource();

	/**
	 * Obtains the target {@link AdaptedChild}.
	 * 
	 * @return Target {@link AdaptedChild}.
	 */
	AdaptedChild<?> getTarget();

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