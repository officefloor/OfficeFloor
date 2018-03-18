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
package net.officefloor.eclipse.editor.models;

import java.util.List;
import java.util.function.Function;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link Model} to {@link ConnectionModel}.
 */
public class ModelToConnection<M extends Model, E extends Enum<E>, C extends ConnectionModel> {

	/**
	 * Obtains the {@link ConnectionModel} instances.
	 */
	private final Function<M, List<C>> getConnections;

	/**
	 * {@link Enum} events to indicate change in {@link ConnectionModel} instances.
	 */
	private final E[] connectionChangeEvents;

	/**
	 * Instantiate.
	 * 
	 * @param getConnections
	 *            Obtains the {@link ConnectionModel} instances.
	 * @param connectionChangeEvents
	 *            {@link Enum} events to indicate change in {@link ConnectionModel}
	 *            instances.
	 */
	public ModelToConnection(Function<M, List<C>> getConnections, E[] connectionChangeEvents) {
		this.getConnections = getConnections;
		this.connectionChangeEvents = connectionChangeEvents;
	}

	/**
	 * Obtains the {@link ConnectionModel} instances.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return {@link ConnectionModel} instances.
	 */
	public List<C> getConnections(M model) {
		return this.getConnections.apply(model);
	}

}