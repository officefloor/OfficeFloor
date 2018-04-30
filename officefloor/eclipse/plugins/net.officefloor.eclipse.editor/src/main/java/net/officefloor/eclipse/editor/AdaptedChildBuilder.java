/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor;

import java.net.URL;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.Property;
import javafx.scene.Parent;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;

/**
 * Builds an {@link AdaptedChild}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedChildBuilder<R extends Model, O, M extends Model, E extends Enum<E>> {

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
	 * Registers a read-only label for the {@link Model}.
	 * 
	 * @param getLabel
	 *            {@link Function} to get the label from the {@link Model}.
	 * @param labelChangeEvents
	 *            {@link Enum} events fired by the {@link Model} for label changes.
	 */
	@SuppressWarnings("unchecked")
	void label(Function<M, String> getLabel, E... labelChangeEvents);

	/**
	 * Registers a mutable label for the {@link Model}.
	 * 
	 * @param getLabel
	 *            {@link Function} to get the label from the {@link Model}.
	 * @param setLabel
	 *            {@link LabelChange}.
	 * @param labelChangeEvents
	 *            {@link Enum} events fired by the {@link Model} for label changes.
	 */
	@SuppressWarnings("unchecked")
	void label(Function<M, String> getLabel, LabelChange<M> setLabel, E... labelChangeEvents);

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
	 * Registers children for the {@link Model}.
	 * 
	 * @param childModelClass
	 *            {@link Model} {@link Class}.
	 * @param getChildren
	 *            {@link Function} to get the children from the {@link Model}.
	 * @param childrenEvents
	 *            {@link Enum} events fired by the {@link Model} for children
	 *            changes.
	 */
	@SuppressWarnings("unchecked")
	ChildrenGroupBuilder<R, O> children(String childGroupName, Function<M, List<? extends Model>> getChildren,
			E... childrenEvents);

	/**
	 * Registers a {@link ConnectionModel} from this {@link AdaptedModel}.
	 * 
	 * @param connectionClass
	 *            {@link ConnectionModel} {@link Class}.
	 * @param getConnection
	 *            {@link Function} to get the {@link ConnectionModel} from the
	 *            {@link Model}.
	 * @param getSource
	 *            {@link Function} to get the source {@link Model} from the
	 *            {@link ConnectionModel}.
	 * @param connectionChangeEvents
	 *            {@link Enum} events fired by the model for {@link ConnectionModel}
	 *            change.
	 * @return {@link AdaptedConnectionBuilder} for the {@link ConnectionModel}.
	 */
	@SuppressWarnings("unchecked")
	<C extends ConnectionModel> AdaptedConnectionBuilder<R, O, M, C, E> connectOne(Class<C> connectionClass,
			Function<M, C> getConnection, Function<C, M> getSource, E... connectionChangeEvents);

	/**
	 * Registers multiple {@link ConnectionModel} from this {@link AdaptedModel}.
	 * 
	 * @param connectionClass
	 *            {@link ConnectionModel} class.
	 * @param getConnections
	 *            {@link Function} to get the {@link List} of
	 *            {@link ConnectionModel} instances from the {@link Model}.
	 * @param getSource
	 *            {@link Function} to get the source {@link Model} from the
	 *            {@link ConnectionModel}.
	 * @param connectionChangeEvents
	 * @return {@link AdaptedConnectionBuilder} for the {@link ConnectionModel}.
	 */
	@SuppressWarnings("unchecked")
	<C extends ConnectionModel> AdaptedConnectionBuilder<R, O, M, C, E> connectMany(Class<C> connectionClass,
			Function<M, List<C>> getConnections, Function<C, M> getSource, E... connectionChangeEvents);

	/**
	 * Creates a {@link Change} for the label of the {@link Model}.
	 */
	public static interface LabelChange<M extends Model> {

		/**
		 * Creates {@link Change} for the label of the {@link Model}.
		 * 
		 * @param model
		 *            {@link Model}.
		 * @param newLabel
		 *            New label.
		 * @return {@link Change}.
		 */
		Change<M> changeLabel(Model model, String newLabel);
	}

}