/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Action containing the details of a delete.
 * 
 * @author Daniel Sagenschneider
 */
public class RemoveConnectionsAction<M extends Model> {

	/**
	 * Method name on the models to remove the connections.
	 */
	public static final String REMOVE_CONNECTIONS_METHOD_NAME = "removeConnections";

	/**
	 * {@link Model} for which {@link ConnectionModel} instances are being
	 * removed.
	 */
	private final M model;

	/**
	 * {@link ConnectionModel} instances removed.
	 */
	private final List<ConnectionModel> connections = new LinkedList<ConnectionModel>();

	/**
	 * Children {@link RemoveConnectionsAction} instances that are part of a
	 * cascade remove of {@link ConnectionModel} instances.
	 */
	private final List<RemoveConnectionsAction<?>> cascadeChildren = new LinkedList<RemoveConnectionsAction<?>>();

	/**
	 * Initiate.
	 * 
	 * @param model
	 *            {@link Model} for which {@link ConnectionModel} instances are
	 *            being removed.
	 */
	public RemoveConnectionsAction(M model) {
		this.model = model;
	}

	/**
	 * Obtains the {@link Model} for which {@link ConnectionModel} instances are
	 * being removed.
	 * 
	 * @return {@link Model} for which {@link ConnectionModel} instances are
	 *         being removed.
	 */
	public M getModel() {
		return this.model;
	}

	/**
	 * Removes the {@link ConnectionModel} and holds reference to it for undo
	 * action.
	 * 
	 * @param connectionModel
	 *            {@link ConnectionModel} and may be <code>null</code> for
	 *            easier coding.
	 */
	public void disconnect(ConnectionModel connectionModel) {

		// Ensure have a connection
		if (connectionModel == null) {
			return;
		}

		// Remove the connection
		connectionModel.remove();

		// Add to listing of connections removed
		this.connections.add(connectionModel);
	}

	/**
	 * Removes the {@link ConnectionModel} instances within the input
	 * {@link Collection}.
	 * 
	 * @param <C>
	 *            {@link ConnectionModel} type.
	 * @param connectionModels
	 *            {@link ConnectionModel} instances.
	 */
	public <C extends ConnectionModel> void disconnect(
			Collection<C> connectionModels) {

		// Ensure have a listing of connections
		if (connectionModels == null) {
			return;
		}

		// Remove the connections
		// Snap shot list to stop concurrent modifications
		for (C connectionModel : new ArrayList<C>(connectionModels)) {
			this.disconnect(connectionModel);
		}
	}

	/**
	 * <p>
	 * Adds a cascade {@link Model}.
	 * <p>
	 * Added cascade {@link Model} instances are reconnected on this being
	 * reconnected.
	 * 
	 * @param <R>
	 *            Model type.
	 * @param child
	 *            Cascade {@link Model}.
	 */
	public <R extends Model> void addCascadeModel(
			RemoveConnectionsAction<R> child) {
		this.cascadeChildren.add(child);
	}

	/**
	 * Reconnects all the {@link ConnectionModel} instances.
	 */
	public void reconnect() {

		// Add the cascade children connections first
		for (RemoveConnectionsAction<?> child : this.cascadeChildren) {
			child.reconnect();
		}

		// Re-connect this model
		for (ConnectionModel connection : this.connections) {
			connection.connect();
		}
	}
}
