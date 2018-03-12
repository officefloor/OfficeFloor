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
package net.officefloor.eclipse.editor.model;

import java.util.List;
import java.util.function.Function;

import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.ChildGroupBuilder;
import net.officefloor.eclipse.editor.ViewFactory;
import net.officefloor.model.Model;

/**
 * Factory to create a
 * 
 * @author Daniel Sagenschneider
 */
public class ChildrenGroupFactory<M extends Model, E extends Enum<E>> implements ChildGroupBuilder {

	/**
	 * Children group name.
	 */
	private final String childGroupName;

	/**
	 * {@link Function} to get the children {@link Model} instances from the
	 * {@link Model} for this group.
	 */
	private final Function<M, List<? extends Model>> getChildren;

	/**
	 * {@link Enum} events fired by the {@link Model} for output {@link Model}
	 * changes.
	 */
	private final E[] childrenEvents;

	/**
	 * Parent {@link AbstractAdaptedModelFactory}.
	 */
	private final AbstractAdaptedModelFactory<?, ?, ?> parentAdaptedModel;

	/**
	 * Instantiate.
	 * 
	 * @param childGroupName
	 *            Child grouping name.
	 * @param getChildren
	 *            {@link Function} to get the children {@link Model} instances from
	 *            the {@link Model}.
	 * @param childrenEvents
	 *            {@link Enum} events fired by the {@link Model} for output
	 *            {@link Model} changes.
	 * @param parentAdaptedModel
	 *            Parent {@link AbstractAdaptedModelFactory}.
	 */
	public ChildrenGroupFactory(String childGroupName, Function<M, List<? extends Model>> getChildren,
			E[] childrenEvents, AbstractAdaptedModelFactory<?, ?, ?> parentAdaptedModel) {
		this.childGroupName = childGroupName;
		this.getChildren = getChildren;
		this.childrenEvents = childrenEvents;
		this.parentAdaptedModel = parentAdaptedModel;
	}

	/**
	 * Creates the {@link ChildrenGroup} for the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model}.
	 * @return {@link ChildrenGroup}.
	 */
	public ChildrenGroup<M, E> createChildrenGroup(M model) {
		return new ChildrenGroup<>(model, this);
	}

	/*
	 * ===================== ChildGroupBuilder ==========================
	 */

	@Override
	public <CM extends Model, CE extends Enum<CE>> AdaptedChildBuilder<CM, CE> addChild(Class<CM> modelClass,
			ViewFactory<CM, AdaptedChild<CM>> viewFactory) {
		return new AdaptedChildModelFactory<>(modelClass, viewFactory, this.parentAdaptedModel);
	}

	/**
	 * Children group.
	 */
	public static class ChildrenGroup<M extends Model, E extends Enum<E>> {

		/**
		 * {@link Model}.
		 */
		private final M model;

		/**
		 * {@link ChildrenGroupFactory}.
		 */
		private final ChildrenGroupFactory<M, E> factory;

		/**
		 * {@link Pane}.
		 */
		private Pane pane = null;

		/**
		 * Instantiate.
		 * 
		 * @param model
		 *            {@link Model}.
		 * @param factory
		 *            {@link ChildrenGroupFactory}.
		 */
		private ChildrenGroup(M model, ChildrenGroupFactory<M, E> factory) {
			this.model = model;
			this.factory = factory;
		}

		/**
		 * Obtains the {@link ChildrenGroup} name.
		 * 
		 * @return {@link ChildrenGroup} name.
		 */
		public String getChildrenGroupName() {
			return this.factory.childGroupName;
		}

		/**
		 * Obtains the {@link Model}.
		 * 
		 * @return {@link Model}.
		 */
		public M getModel() {
			return this.model;
		}

		/**
		 * Obtains the events.
		 * 
		 * @return Events.
		 */
		public E[] getEvents() {
			return this.factory.childrenEvents;
		}

		/**
		 * Obtains the children {@link Model} instances.
		 * 
		 * @return Children {@link Model} instances.
		 */
		public List<? extends Model> getChildrenModels() {
			return this.factory.getChildren.apply(this.model);
		}

		/**
		 * Specifies the {@link Pane}.
		 * 
		 * @param pane
		 *            {@link Pane}.
		 */
		public void setPane(Pane pane) {
			this.pane = pane;
		}

		/**
		 * Obtains the {@link Pane} for the children {@link Model} instances.
		 * 
		 * @return {@link Pane} for the children {@link Model} instances.
		 */
		public Pane getPane() {
			return this.pane;
		}
	}

}