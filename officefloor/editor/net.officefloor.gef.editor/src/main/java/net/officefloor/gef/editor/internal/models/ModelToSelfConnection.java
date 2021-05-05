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
