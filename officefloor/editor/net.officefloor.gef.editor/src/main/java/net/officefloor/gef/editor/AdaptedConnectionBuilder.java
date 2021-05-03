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

import java.util.List;
import java.util.function.Function;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Builder for the {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectionBuilder<R extends Model, O, S extends Model, C extends ConnectionModel, E extends Enum<E>> {

	/**
	 * Provides linking to the target {@link ConnectionModel}.
	 * 
	 * @param <T>
	 *            Target {@link Model} type.
	 * @param <TE>
	 *            Target {@link Model} event type.
	 * @param targetModel
	 *            Target {@link Model} type.
	 * @param getConnection
	 *            {@link Function} to obtain the {@link ConnectionModel} from the
	 *            target {@link Model}.
	 * @param getTarget
	 *            {@link Function} to extract the target {@link Model} from the
	 *            {@link ConnectionModel}.
	 * @param targetChangeEvents
	 *            {@link Enum} events on the target {@link Model} indicating change
	 *            in {@link ConnectionModel}.
	 * @return <code>this</code>.
	 */
	@SuppressWarnings("unchecked")
	<T extends Model, TE extends Enum<TE>> AdaptedConnectionManagementBuilder<R, O, S, C, T> toOne(Class<T> targetModel,
			Function<T, C> getConnection, Function<C, T> getTarget, TE... targetChangeEvents);

	/**
	 * Provides linking to the target {@link ConnectionModel}.
	 * 
	 * @param <T>
	 *            Target {@link Model} type.
	 * @param <TE>
	 *            Target {@link Model} event type.
	 * @param targetModel
	 *            Target {@link Model} type.
	 * @param getConnections
	 *            {@link Function} to obtain the {@link ConnectionModel} instances
	 *            from the target {@link Model}.
	 * @param getTarget
	 *            {@link Function} to extract the target {@link Model} from the
	 *            {@link ConnectionModel}.
	 * @param targetChangeEvents
	 *            {@link Enum} events on the target {@link Model} indicating change
	 *            in {@link ConnectionModel}.
	 * @return {@link AdaptedConnectionManagementBuilder}.
	 */
	@SuppressWarnings("unchecked")
	<T extends Model, TE extends Enum<TE>> AdaptedConnectionManagementBuilder<R, O, S, C, T> toMany(
			Class<T> targetModel, Function<T, List<C>> getConnections, Function<C, T> getTarget,
			TE... targetChangeEvents);

}
