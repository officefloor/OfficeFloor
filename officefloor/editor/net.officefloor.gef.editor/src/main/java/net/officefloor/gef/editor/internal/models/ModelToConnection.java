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
