/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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
package net.officefloor.eclipse.editor.internal.models;

import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted connector.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnector<M extends Model> {

	/**
	 * Parent {@link AdaptedChild} containing this {@link AdaptedConnector}.
	 */
	private final AdaptedChild<M> parentAdaptedChild;

	/**
	 * {@link ConnectionModel} {@link Class}.
	 */
	private final Class<? extends ConnectionModel> connectionClass;

	/**
	 * Instantiate.
	 * 
	 * @param parentAdaptedChild
	 *            Parent {@link AdaptedChild} containing this
	 *            {@link AdaptedConnector}.
	 * @param connectionClass
	 *            {@link ConnectionModel} {@link Class}.
	 */
	public AdaptedConnector(AdaptedChild<M> parentAdaptedChild, Class<? extends ConnectionModel> connectionClass) {
		this.parentAdaptedChild = parentAdaptedChild;
		this.connectionClass = connectionClass;
	}

	/**
	 * Obtains the parent {@link AdaptedChild}.
	 * 
	 * @return Parent {@link AdaptedChild}.
	 */
	public AdaptedChild<M> getParentAdaptedChild() {
		return this.parentAdaptedChild;
	}

	/**
	 * Obtains the {@link ConnectionModel} {@link Class}.
	 * 
	 * @return {@link ConnectionModel} {@link Class}.
	 */
	public Class<? extends ConnectionModel> getConnectionModelClass() {
		return this.connectionClass;
	}

}