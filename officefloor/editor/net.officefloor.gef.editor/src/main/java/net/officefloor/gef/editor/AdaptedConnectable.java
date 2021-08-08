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

import java.net.URL;
import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import net.officefloor.gef.editor.internal.models.AdaptedConnectorImpl;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectable<M extends Model> extends AdaptedModel<M> {

	/**
	 * Obtains the {@link AdaptedConnectorImpl} instances.
	 * 
	 * @return {@link AdaptedConnectorImpl} instances.
	 */
	List<AdaptedConnector<M>> getAdaptedConnectors();

	/**
	 * Obtains the {@link AdaptedConnectorImpl}.
	 * 
	 * @param connectionClass {@link ConnectionModel} {@link Class}.
	 * @param type            {@link AdaptedConnectorRole}.
	 * @return {@link AdaptedConnectorImpl}.
	 */
	AdaptedConnector<M> getAdaptedConnector(Class<? extends ConnectionModel> connectionClass,
			AdaptedConnectorRole type);

	/**
	 * Obtains the {@link AdaptedConnection} instances of this
	 * {@link AdaptedConnectable} and all its {@link AdaptedConnectable} instances.
	 * 
	 * @return {@link AdaptedConnection} instances.
	 */
	List<AdaptedConnection<?>> getConnections();

	/**
	 * Obtains the {@link AdaptedPotentialConnection} to the target.
	 * 
	 * @param <T>    Target {@link Model} type.
	 * @param target Target {@link AdaptedConnectable},
	 * @return {@link AdaptedPotentialConnection} to the target or <code>null</code>
	 *         if no means to connect to target.
	 */
	<T extends Model> AdaptedPotentialConnection getPotentialConnection(AdaptedConnectable<T> target);

	/**
	 * Creates the {@link ConnectionModel} within the {@link Model} structure.
	 * 
	 * @param <T>        Target {@link Model} type.
	 * @param target     Target {@link AdaptedConnectable}.
	 * @param sourceRole {@link AdaptedConnectorRole} of the this source
	 *                   {@link AdaptedConnectable}.
	 */
	<T extends Model> void createConnection(AdaptedConnectable<T> target, AdaptedConnectorRole sourceRole);

	/**
	 * Obtains the {@link Property} to the style sheet for this
	 * {@link AdaptedConnectable}.
	 * 
	 * @return {@link Property} to the style sheet for this
	 *         {@link AdaptedConnectable}.
	 */
	Property<String> getStylesheet();

	/**
	 * <p>
	 * Obtains the {@link Property} to the style sheet URL for this visual of this
	 * {@link AdaptedConnectable}.
	 * <p>
	 * May be <code>null</code> to indicate no specific styling.
	 * 
	 * @return {@link ReadOnlyProperty} to the style sheet {@link URL}. May be
	 *         <code>null</code>.
	 */
	ReadOnlyProperty<URL> getStylesheetUrl();

	/**
	 * Undertakes the {@link ModelAction}.
	 *
	 * @param <R>    Root {@link Model} type.
	 * @param <O>    Operations type.
	 * @param action {@link ModelAction}.
	 */
	<R extends Model, O> void action(ModelAction<R, O, M> action);

	/**
	 * Obtains the {@link AdaptedErrorHandler}.
	 * 
	 * @return {@link AdaptedErrorHandler}.
	 */
	AdaptedErrorHandler getErrorHandler();

	/**
	 * Obtains the the drag latency.
	 * 
	 * @return Drag latency.
	 */
	int getDragLatency();

	/**
	 * Obtains whether {@link SelectOnly}.
	 * 
	 * @return {@link SelectOnly} or <code>null</code> to allow functionality.
	 */
	SelectOnly getSelectOnly();

}
