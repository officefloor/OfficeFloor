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

import org.eclipse.gef.fx.anchors.IAnchor;
import org.eclipse.gef.fx.nodes.GeometryNode;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Context for the {@link AdaptedChildVisualFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedModelVisualFactoryContext<M extends Model> extends AdaptedConnectorVisualFactoryContext {

	/**
	 * <p>
	 * Add the {@link Node} to the parent {@link Pane} returning it.
	 * <p>
	 * This allows for convenient adding new {@link Node} instances to {@link Pane}.
	 *
	 * @param <N>    {@link Node} type.
	 * @param parent Parent {@link Pane}.
	 * @param node   {@link Node}.
	 * @return Input {@link Node}
	 */
	<N extends Node> N addNode(Pane parent, N node);

	/**
	 * Adds an indent {@link Pane}.
	 * 
	 * @param parent Parent {@link HBox}.
	 * @return {@link Pane} for the indent.
	 */
	Pane addIndent(HBox parent);

	/**
	 * Connector.
	 */
	public static interface Connector {

		/**
		 * Obtains the {@link Node} for the {@link Connector}.
		 * 
		 * @return {@link Node} for the {@link Connector}.
		 */
		Node getNode();

		/**
		 * Qualifies as source {@link Connector} for {@link ConnectionModel} connecting
		 * itself.
		 * 
		 * @param sourceConnectionModelClasses Source {@link ConnectionModel}
		 *                                     {@link Class} instances for self
		 *                                     connecting.
		 * @return Source {@link Connector}.
		 */
		@SuppressWarnings("rawtypes")
		Connector source(Class... sourceConnectionModelClasses);

		/**
		 * Qualifies as target {@link Connector} for {@link ConnectionModel} connecting
		 * itself.
		 * 
		 * @param targetConnectionModelClasses Target {@link ConnectionModel}
		 *                                     {@link Class} instances for self
		 *                                     connecting.
		 * @return Target {@link Connector}.
		 */
		@SuppressWarnings("rawtypes")
		Connector target(Class... targetConnectionModelClasses);
	}

	/**
	 * Specifies the {@link GeometryNode} as {@link AdaptedConnector}
	 * {@link IAnchor}.
	 *
	 * @param <N>                    {@link Node} type.
	 * @param visualFactory          {@link AdaptedConnectorVisualFactory}.
	 * @param connectionModelClasses {@link ConnectionModel} {@link Class} instances
	 *                               that this connector satisfies.
	 * @return {@link Connector}.
	 * 
	 * @see DefaultConnectors
	 */
	@SuppressWarnings("rawtypes")
	<N extends Region> Connector connector(AdaptedConnectorVisualFactory<N> visualFactory,
			Class... connectionModelClasses);

	/**
	 * Convenience method to create a {@link Node} with {@link Image} and hover
	 * {@link Image}. Typically this is to create button for the action.
	 * 
	 * @param resourceClass      {@link Class} within the class path containing the
	 *                           images.
	 * @param imageFilePath      Path to the {@link Image}.
	 * @param hoverImageFilePath Path to the hover {@link Image}.
	 * @return {@link Node} for the {@link Image} with hover.
	 * 
	 * @see DefaultImages
	 */
	Node createImageWithHover(Class<?> resourceClass, String imageFilePath, String hoverImageFilePath);

	/**
	 * <p>
	 * Allows {@link ModelAction} instances to be actioned via visual.
	 * <p>
	 * This allows custom visuals (e.g. button) tor trigger an action.
	 *
	 * @param <R>    Root {@link Model} type.
	 * @param <O>    Operations type.
	 * @param action {@link ModelAction} to be actioned.
	 */
	<R extends Model, O> void action(ModelAction<R, O, M> action);

	/**
	 * Convenience method to add a {@link ModelAction} to be actioned via
	 * {@link AdaptedActionVisualFactory}.
	 *
	 * @param <R>           Root {@link Model} type.
	 * @param <O>           Operations type.
	 * @param action        {@link ModelAction} to be actioned.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 * @return {@link Node} to trigger the {@link ModelAction}.
	 */
	<R extends Model, O> Node action(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory);

}
