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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * {@link Model} that has a {@link ConnectionModel} to itself (though may be
 * another instance).
 */
public class ModelToSelfConnection<R extends Model, O, M extends Model, E extends Enum<E>, C extends ConnectionModel>
		extends ModelToConnection<R, O, M, E, C> {

	/**
	 * Combines the change events.
	 * 
	 * @param sourceToConnection
	 *            Source {@link ModelToConnection}.
	 * @param targetToConnection
	 *            Target {@link ModelToConnection}.
	 * @return Combined change events.
	 */
	@SuppressWarnings("unchecked")
	private static <R extends Model, O, M extends Model, E extends Enum<E>, C extends ConnectionModel> E[] combineEvents(
			ModelToConnection<R, O, M, E, C> sourceToConnection, ModelToConnection<R, O, M, E, C> targetToConnection) {
		E[] sourceEvents = sourceToConnection.connectionChangeEvents;
		E[] targetEvents = targetToConnection.connectionChangeEvents;
		E[] allEvents = (E[]) Array.newInstance(sourceEvents.getClass().getComponentType(),
				sourceEvents.length + targetEvents.length);
		for (int i = 0; i < sourceEvents.length; i++) {
			allEvents[i] = sourceEvents[i];
		}
		for (int i = 0; i < targetEvents.length; i++) {
			allEvents[sourceEvents.length + i] = targetEvents[i];
		}
		return allEvents;
	}

	/**
	 * Source {@link ModelToConnection}.
	 */
	private final ModelToConnection<R, O, M, E, C> sourceToConnection;

	/**
	 * Target {@link ModelToConnection}.
	 */
	private final ModelToConnection<R, O, M, E, C> targetToConnection;

	/**
	 * Instantiate.
	 * 
	 * @param sourceToConnection
	 *            Source {@link ModelToConnection}.
	 * @param targetToConnection
	 *            Target {@link ModelToConnection}.
	 */
	public ModelToSelfConnection(ModelToConnection<R, O, M, E, C> sourceToConnection,
			ModelToConnection<R, O, M, E, C> targetToConnection) {
		super((m) -> {
			// Obtain the listing of connections
			List<C> sourceConnections = sourceToConnection.getConnections.apply(m);
			List<C> targetConnections = targetToConnection.getConnections.apply(m);
			List<C> allConnections = new ArrayList<>(sourceConnections.size() + targetConnections.size());
			allConnections.addAll(sourceConnections);
			allConnections.addAll(targetConnections);
			return allConnections;
		}, combineEvents(sourceToConnection, targetToConnection), sourceToConnection.adaptedConnectionFactory);
		this.sourceToConnection = sourceToConnection;
		this.targetToConnection = targetToConnection;
	}

	/**
	 * Obtains the source {@link ModelToConnection}.
	 * 
	 * @return Source {@link ModelToConnection}.
	 */
	public ModelToConnection<R, O, M, E, C> getSourceToConnection() {
		return this.sourceToConnection;
	}

	/**
	 * Obtains the target {@link ModelToConnection}.
	 * 
	 * @return Target {@link ModelToConnection}.
	 */
	public ModelToConnection<R, O, M, E, C> getTargetToConnection() {
		return this.targetToConnection;
	}

}
