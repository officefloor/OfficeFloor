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
package net.officefloor.eclipse.editor.models;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedModel;
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
	 * Parent {@link AbstractAdaptedFactory}.
	 */
	private final AbstractAdaptedFactory<?, ?, ?> parentAdaptedModel;

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
	 *            Parent {@link AbstractAdaptedFactory}.
	 */
	public ChildrenGroupFactory(String childGroupName, Function<M, List<? extends Model>> getChildren,
			E[] childrenEvents, AbstractAdaptedFactory<?, ?, ?> parentAdaptedModel) {
		this.childGroupName = childGroupName;
		this.getChildren = getChildren;
		this.childrenEvents = childrenEvents;
		this.parentAdaptedModel = parentAdaptedModel;
	}

	/**
	 * Creates the {@link ChildrenGroup} for the parent {@link AdaptedChild}.
	 * 
	 * @param parent
	 *            Parent {@link AdaptedChild}.
	 * @return {@link ChildrenGroup}.
	 */
	public ChildrenGroup<M, E> createChildrenGroup(AdaptedChild<M> parent) {
		return new ChildrenGroup<>(parent, this);
	}

	/*
	 * ===================== ChildGroupBuilder ==========================
	 */

	@Override
	public <CM extends Model, CE extends Enum<CE>> AdaptedChildBuilder<CM, CE> addChild(Class<CM> modelClass,
			ViewFactory<CM, AdaptedChild<CM>> viewFactory) {
		return new AdaptedChildFactory<>(modelClass, viewFactory, this.parentAdaptedModel);
	}

	/**
	 * Children group.
	 */
	public static class ChildrenGroup<M extends Model, E extends Enum<E>> {

		/**
		 * Parent {@link AdaptedChild}.
		 */
		private final AdaptedChild<M> parent;

		/**
		 * {@link ChildrenGroupFactory}.
		 */
		private final ChildrenGroupFactory<M, E> factory;

		/**
		 * {@link AdaptedModel} children.
		 */
		private final List<AdaptedModel<?>> children;

		/**
		 * {@link Pane}.
		 */
		private Pane pane = null;

		/**
		 * Instantiate.
		 * 
		 * @param parent
		 *            Parent {@link AdaptedChild}.
		 * @param factory
		 *            {@link ChildrenGroupFactory}.
		 */
		private ChildrenGroup(AdaptedChild<M> parent, ChildrenGroupFactory<M, E> factory) {
			this.parent = parent;
			this.factory = factory;

			// Load the children
			List<? extends Model> children = this.factory.getChildren.apply(this.parent.getModel());
			this.children = new ArrayList<>(children.size());
			for (Model child : children) {
				AdaptedModel<?> adaptedChild = this.factory.parentAdaptedModel.getAdaptedModel(child);
				this.children.add(adaptedChild);
			}
		}

		/**
		 * Obtains the parent {@link AdaptedChild}.
		 * 
		 * @return Parent {@link AdaptedChild}.
		 */
		public AdaptedChild<M> getParent() {
			return this.parent;
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
		 * Obtains the {@link AdaptedModel} children.
		 * 
		 * @return {@link AdaptedModel} children.
		 */
		public List<AdaptedModel<?>> getChildren() {
			return this.children;
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