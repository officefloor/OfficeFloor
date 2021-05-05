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

package net.officefloor.gef.editor.internal.models;

import java.util.List;
import java.util.function.Function;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link Model} to {@link ConnectionModel}.
 */
public class ModelToConnection<R extends Model, O, M extends Model, E extends Enum<E>, C extends ConnectionModel> {

	/**
	 * Obtains the {@link ConnectionModel} instances.
	 */
	protected final Function<M, List<C>> getConnections;

	/**
	 * {@link Enum} events to indicate change in {@link ConnectionModel} instances.
	 */
	protected final E[] connectionChangeEvents;

	/**
	 * {@link AdaptedConnectionFactory} to create the {@link ConnectionModel} for
	 * the {@link Model}.
	 */
	protected final AdaptedConnectionFactory<R, O, ?, ?, ?> adaptedConnectionFactory;

	/**
	 * Instantiate.
	 * 
	 * @param getConnections
	 *            Obtains the {@link ConnectionModel} instances.
	 * @param connectionChangeEvents
	 *            {@link Enum} events to indicate change in {@link ConnectionModel}
	 *            instances.
	 * @param adaptedConnectionFactory
	 *            {@link AdaptedConnectionFactory} to create the
	 *            {@link ConnectionModel}.
	 */
	public ModelToConnection(Function<M, List<C>> getConnections, E[] connectionChangeEvents,
			AdaptedConnectionFactory<R, O, ?, ?, ?> adaptedConnectionFactory) {
		this.getConnections = getConnections;
		this.connectionChangeEvents = connectionChangeEvents;
		this.adaptedConnectionFactory = adaptedConnectionFactory;
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

	/**
	 * Obtains the {@link ConnectionModel} change event {@link Enum} instances.
	 * 
	 * @return {@link ConnectionModel} change event {@link Enum} instances.
	 */
	public E[] getConnectionChangeEvents() {
		return this.connectionChangeEvents;
	}

	/**
	 * Obtains the {@link AdaptedConnectionFactory} for the {@link ConnectionModel}.
	 * 
	 * @return {@link AdaptedConnectionFactory} for the {@link ConnectionModel}.
	 */
	public AdaptedConnectionFactory<R, O, ?, ?, ?> getAdaptedConnectionFactory() {
		return this.adaptedConnectionFactory;
	}

}
