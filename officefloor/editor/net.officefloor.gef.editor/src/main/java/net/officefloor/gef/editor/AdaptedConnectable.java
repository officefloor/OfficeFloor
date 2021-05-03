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
