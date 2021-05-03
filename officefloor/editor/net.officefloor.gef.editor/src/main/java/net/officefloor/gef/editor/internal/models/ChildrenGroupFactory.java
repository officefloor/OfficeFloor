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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.eclipse.gef.common.collections.CollectionUtils;

import javafx.collections.ObservableList;
import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.AdaptedChildBuilder;
import net.officefloor.gef.editor.AdaptedChildVisualFactory;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.ChildrenGroupBuilder;
import net.officefloor.model.Model;

/**
 * Factory to create a
 * 
 * @author Daniel Sagenschneider
 */
public class ChildrenGroupFactory<R extends Model, O, M extends Model, E extends Enum<E>>
		implements ChildrenGroupBuilder<R, O> {

	/**
	 * Configuration path.
	 */
	private final String configurationPath;

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
	private final AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedModel;

	/**
	 * Instantiate.
	 * 
	 * @param configurationPathPrefix
	 *            Prefix to the configuration path.
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
	public ChildrenGroupFactory(String configurationPathPrefix, String childGroupName,
			Function<M, List<? extends Model>> getChildren, E[] childrenEvents,
			AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedModel) {
		this.configurationPath = configurationPathPrefix + "." + childGroupName;
		this.childGroupName = childGroupName;
		this.getChildren = getChildren;
		this.childrenEvents = childrenEvents;
		this.parentAdaptedModel = parentAdaptedModel;
	}

	/**
	 * Creates the {@link ChildrenGroupImpl} for the parent {@link AdaptedChild}.
	 * 
	 * @param parent
	 *            Parent {@link AdaptedChild}.
	 * @return {@link ChildrenGroupImpl}.
	 */
	public ChildrenGroup<M, E> createChildrenGroup(AdaptedChild<M> parent) {
		return new ChildrenGroupImpl<>(parent, this);
	}

	/*
	 * ===================== ChildGroupBuilder ==========================
	 */

	@Override
	public String getConfigurationPath() {
		return this.configurationPath;
	}

	@Override
	public <CM extends Model, CE extends Enum<CE>> AdaptedChildBuilder<R, O, CM, CE> addChild(CM modelPrototype,
			AdaptedChildVisualFactory<CM> viewFactory) {
		return new AdaptedChildFactory<>(this.configurationPath, modelPrototype, viewFactory, this.parentAdaptedModel);
	}

	/**
	 * Children group.
	 */
	public static class ChildrenGroupImpl<R extends Model, O, M extends Model, E extends Enum<E>>
			implements ChildrenGroup<M, E> {

		/**
		 * Parent {@link AdaptedChild}.
		 */
		private final AdaptedChild<M> parent;

		/**
		 * {@link ChildrenGroupFactory}.
		 */
		private final ChildrenGroupFactory<? extends Model, ?, M, E> factory;

		/**
		 * {@link AdaptedChild} instances.
		 */
		private final ObservableList<AdaptedChild<?>> children;

		/**
		 * Instantiate.
		 * 
		 * @param parent
		 *            Parent {@link AdaptedChild}.
		 * @param factory
		 *            {@link ChildrenGroupFactory}.
		 */
		private ChildrenGroupImpl(AdaptedChild<M> parent, ChildrenGroupFactory<? extends Model, ?, M, E> factory) {
			this.parent = parent;
			this.factory = factory;

			// Enable re-loading the children
			this.children = CollectionUtils.observableArrayList();
			Runnable loadChildren = () -> {
				List<? extends Model> children = this.factory.getChildren.apply(this.parent.getModel());
				List<AdaptedChild<?>> adaptedChildren = new ArrayList<>(children.size());
				for (Model child : children) {
					AdaptedChild<?> adaptedChild = (AdaptedChild<?>) this.factory.parentAdaptedModel
							.getAdaptedModel(child, this.parent);
					adaptedChildren.add(adaptedChild);
				}
				this.children.setAll(adaptedChildren);
			};

			// Load the children
			loadChildren.run();

			// Listen for changes in children (and reload children)
			AdaptedChildFactory.registerEventListener(this.parent.getModel(), this.factory.childrenEvents,
					(event) -> loadChildren.run());
		}

		/*
		 * ================= ChildrenGroup =================
		 */

		@Override
		public AdaptedChild<M> getParent() {
			return this.parent;
		}

		@Override
		public String getChildrenGroupName() {
			return this.factory.childGroupName;
		}

		@Override
		public ObservableList<AdaptedChild<?>> getChildren() {
			return this.children;
		}

		@Override
		public E[] getEvents() {
			return this.factory.childrenEvents;
		}
	}

}
