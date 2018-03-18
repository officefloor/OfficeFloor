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
package net.officefloor.eclipse.editor;

import java.util.List;
import java.util.function.Function;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Builder for the {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectionBuilder<S extends Model, C extends ConnectionModel, E extends Enum<E>> {

	/**
	 * Provides linking to the target {@link ConnectionModel}.
	 * 
	 * @param targetModel
	 *            Target {@link Model} type.
	 * @param getConnection
	 *            {@link Function} to obtain the {@link ConnectionModel} from the
	 *            target {@link Model}.
	 * @param getTarget
	 *            {@link Function} to extract the target {@link Model} from the
	 *            {@link ConnectionModel}.
	 * @param createConnection
	 *            {@link ConnectionFactory}.
	 * @param removeConnection
	 *            {@link Function} to remove the {@link ConnectionModel}.
	 * @param targetChangeEvents
	 *            {@link Enum} events on the target {@link Model} indicating change
	 *            in {@link ConnectionModel}.
	 */
	@SuppressWarnings("unchecked")
	<T extends Model, TE extends Enum<TE>> void toOne(Class<T> targetModel, Function<T, C> getConnection,
			Function<C, T> getTarget, ConnectionFactory<S, C, T> createConnection,
			Function<C, Change<C>> removeConnection, TE... targetChangeEvents);

	/**
	 * Provides linking to the target {@link ConnectionModel}.
	 * 
	 * @param targetModel
	 *            Target {@link Model} type.
	 * @param getConnections
	 *            {@link Function} to obtain the {@link ConnectionModel} instances
	 *            from the target {@link Model}.
	 * @param getTarget
	 *            {@link Function} to extract the target {@link Model} from the
	 *            {@link ConnectionModel}.
	 * @param createConnection
	 *            {@link ConnectionFactory}.
	 * @param removeConnection
	 *            {@link Function} to remove the {@link ConnectionModel}.
	 * @param targetChangeEvents
	 *            {@link Enum} events on the target {@link Model} indicating change
	 *            in {@link ConnectionModel}.
	 */
	@SuppressWarnings("unchecked")
	<T extends Model, TE extends Enum<TE>> void toMany(Class<T> targetModel, Function<T, List<C>> getConnections,
			Function<C, T> getTarget, ConnectionFactory<S, C, T> createConnection,
			Function<C, Change<C>> removeConnection, TE... targetChangeEvents);

	/**
	 * {@link Function} interface to create a {@link ConnectionModel}.
	 */
	public static interface ConnectionFactory<S extends Model, C extends ConnectionModel, T extends Model> {

		/**
		 * Adds a {@link ConnectionModel}.
		 * 
		 * @param source
		 *            Source {@link Model}.
		 * @param target
		 *            Target {@link Model}.
		 * @return {@link Change} for the {@link ConnectionModel}.
		 */
		Change<C> addConnection(S source, T target);
	}

}