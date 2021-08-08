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
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.scene.Parent;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Builds an {@link AdaptedChild}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectableBuilder<R extends Model, O, M extends Model, E extends Enum<E>> {

	/**
	 * Obtains the configuration path.
	 * 
	 * @return Configuration path.
	 */
	String getConfigurationPath();

	/**
	 * Obtains the {@link Model} {@link Class}.
	 * 
	 * @return {@link Model} {@link Class}.
	 */
	Class<M> getModelClass();

	/**
	 * <p>
	 * Obtains the {@link Property} to the style sheet rules for the
	 * {@link AdaptedChild}.
	 * <p>
	 * Note: this is <strong>NOT</strong> the style sheet {@link URL}. This is the
	 * style sheet rules (content of style sheet) and the {@link AdaptedChild} will
	 * handle making available to {@link Parent} as a {@link URL}.
	 * 
	 * @return {@link Property} to the style sheet rules.
	 */
	Property<String> style();

	/**
	 * Registers a {@link ConnectionModel} from this {@link AdaptedModel}.
	 * 
	 * @param                        <C> {@link ConnectionModel} type.
	 * @param connectionClass        {@link ConnectionModel} {@link Class}.
	 * @param getConnection          {@link Function} to get the
	 *                               {@link ConnectionModel} from the {@link Model}.
	 * @param getSource              {@link Function} to get the source
	 *                               {@link Model} from the {@link ConnectionModel}.
	 * @param connectionChangeEvents {@link Enum} events fired by the model for
	 *                               {@link ConnectionModel} change.
	 * @return {@link AdaptedConnectionBuilder} for the {@link ConnectionModel}.
	 */
	@SuppressWarnings("unchecked")
	<C extends ConnectionModel> AdaptedConnectionBuilder<R, O, M, C, E> connectOne(Class<C> connectionClass,
			Function<M, C> getConnection, Function<C, M> getSource, E... connectionChangeEvents);

	/**
	 * Registers multiple {@link ConnectionModel} from this {@link AdaptedModel}.
	 *
	 * @param                        <C> {@link ConnectionModel} type.
	 * @param connectionClass        {@link ConnectionModel} class.
	 * @param getConnections         {@link Function} to get the {@link List} of
	 *                               {@link ConnectionModel} instances from the
	 *                               {@link Model}.
	 * @param getSource              {@link Function} to get the source
	 *                               {@link Model} from the {@link ConnectionModel}.
	 * @param connectionChangeEvents {@link Enum} events fired by the model for
	 *                               {@link ConnectionModel} change.
	 * @return {@link AdaptedConnectionBuilder} for the {@link ConnectionModel}.
	 */
	@SuppressWarnings("unchecked")
	<C extends ConnectionModel> AdaptedConnectionBuilder<R, O, M, C, E> connectMany(Class<C> connectionClass,
			Function<M, List<C>> getConnections, Function<C, M> getSource, E... connectionChangeEvents);

}
