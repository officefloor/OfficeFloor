/*-
 * #%L
 * [bundle] OfficeFloor Editor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
