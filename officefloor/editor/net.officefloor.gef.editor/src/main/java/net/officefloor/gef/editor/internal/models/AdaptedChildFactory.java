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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import net.officefloor.gef.editor.AdaptedChild;
import net.officefloor.gef.editor.AdaptedChildBuilder;
import net.officefloor.gef.editor.AdaptedChildVisualFactory;
import net.officefloor.gef.editor.AdaptedChildVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedModel;
import net.officefloor.gef.editor.AdaptedParentBuilder;
import net.officefloor.gef.editor.ChildrenGroup;
import net.officefloor.gef.editor.ChildrenGroupBuilder;
import net.officefloor.gef.editor.SelectOnly;
import net.officefloor.gef.editor.internal.models.ChildrenGroupFactory.ChildrenGroupImpl;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Factory for an {@link AdaptedChild}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedChildFactory<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedChild<M>>
		extends AbstractAdaptedConnectableFactory<R, O, M, E, A> implements AdaptedChildBuilder<R, O, M, E> {

	/**
	 * {@link AdaptedChildVisualFactory}.
	 */
	private final AdaptedChildVisualFactory<M> viewFactory;

	/**
	 * {@link Function} to get the label from the {@link Model}.
	 */
	private Function<M, String> getLabel = null;

	/**
	 * {@link LabelChange}.
	 */
	private LabelChange<M> setLabel = null;

	/**
	 * {@link Enum} events fired by the {@link Model} for label changes.
	 */
	private E[] labelEvents = null;

	/**
	 * Static listing of {@link ChildrenGroupFactory} instances for the
	 * {@link Model}.
	 */
	private final List<ChildrenGroupFactory<R, O, M, E>> childrenGroups = new ArrayList<>();

	/**
	 * Instantiate as {@link AdaptedChild}.
	 * 
	 * @param configurationPathPrefix Prefix on the configuration path.
	 * @param modelPrototype          {@link Model} prototype.
	 * @param viewFactory             {@link AdaptedChildVisualFactory}.
	 * @param parentAdaptedModel      Parent {@link AbstractAdaptedFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedChildFactory(String configurationPathPrefix, M modelPrototype,
			AdaptedChildVisualFactory<M> viewFactory, AbstractAdaptedFactory<R, O, ?, ?, ?> parentAdaptedModel) {
		super(configurationPathPrefix, modelPrototype, () -> (A) new AdaptedChildImpl<R, O, M, E, AdaptedChild<M>>(),
				parentAdaptedModel);
		this.viewFactory = viewFactory;
	}

	/**
	 * Allow {@link AdaptedParentBuilder} inheritance.
	 * 
	 * @param configurationPathPrefix Prefix on the configuration path.
	 * @param modelPrototype          {@link Model} prototype.
	 * @param newAdaptedModel         {@link Supplier} for the {@link AdaptedModel}.
	 * @param viewFactory             {@link AdaptedChildVisualFactory}.
	 * @param contentPartFactory      {@link OfficeFloorContentPartFactory}.
	 */
	protected AdaptedChildFactory(String configurationPathPrefix, M modelPrototype, Supplier<A> newAdaptedModel,
			AdaptedChildVisualFactory<M> viewFactory, OfficeFloorContentPartFactory<R, O> contentPartFactory) {
		super(configurationPathPrefix, modelPrototype, newAdaptedModel, contentPartFactory);
		this.viewFactory = viewFactory;
	}

	/*
	 * ==================== AdaptedChildBuilder =====================
	 */

	@Override
	@SafeVarargs
	public final void label(Function<M, String> getLabel, E... labelChangeEvents) {
		this.label(getLabel, null, labelChangeEvents);
	}

	@Override
	@SafeVarargs
	public final void label(Function<M, String> getLabel, LabelChange<M> setLabel, E... labelChangeEvents) {
		this.getLabel = getLabel;
		this.setLabel = setLabel;
		this.labelEvents = labelChangeEvents;
	}

	@Override
	@SafeVarargs
	public final ChildrenGroupBuilder<R, O> children(String childGroupName,
			Function<M, List<? extends Model>> getChildren, E... childrenEvents) {
		ChildrenGroupFactory<R, O, M, E> factory = new ChildrenGroupFactory<>(this.getConfigurationPath(),
				childGroupName, getChildren, childrenEvents, this);
		this.childrenGroups.add(factory);
		return factory;
	}

	/**
	 * {@link AdaptedChild} implementation.
	 */
	protected static class AdaptedChildImpl<R extends Model, O, M extends Model, E extends Enum<E>, A extends AdaptedChild<M>>
			extends AbstractAdaptedConnectable<R, O, M, E, A, AdaptedChildFactory<R, O, M, E, A>>
			implements AdaptedChild<M> {

		/**
		 * Label for the {@link Model}.
		 */
		private ReadOnlyStringWrapper label;

		/**
		 * Potential conflict in {@link LabelChange}.
		 */
		private ReadOnlyStringWrapper labelConflict;

		/**
		 * {@link ChildrenGroupImpl} instances.
		 */
		private List<ChildrenGroup<M, ?>> childrenGroups;

		/*
		 * =================== AdaptedChild =====================
		 */

		@Override
		protected void init() {
			super.init();

			// Load the children groups
			this.childrenGroups = new ArrayList<>(this.getFactory().childrenGroups.size());
			for (ChildrenGroupFactory<R, O, M, E> childrenGroupFactory : this.getFactory().childrenGroups) {
				this.childrenGroups.add(childrenGroupFactory.createChildrenGroup(this));
			}

			// Determine if label
			if (this.getFactory().getLabel == null) {
				// No label for model
				this.label = null;
				this.labelConflict = null;

			} else {
				// Create the label conflict property
				this.labelConflict = new ReadOnlyStringWrapper("");

				// Create the edit label property
				String initialLabel = this.getFactory().getLabel.apply(this.getModel());
				this.label = new ReadOnlyStringWrapper(this.getModel(), "Label",
						initialLabel != null ? initialLabel : "");
				if (this.getFactory().setLabel != null) {
					this.label.addListener((lister, oldValue, newValue) -> {

						// Drop out (of potential loop) if changing to same model value
						String currentValue = this.getFactory().getLabel.apply(this.getModel());
						currentValue = currentValue == null ? "" : currentValue;
						newValue = newValue == null ? "" : newValue;
						if (currentValue.equals(newValue)) {
							return;
						}

						// Attempt to change the label
						Change<M> change = this.getFactory().setLabel.changeLabel(this.getModel(), newValue);
						Conflict[] conflicts = change.getConflicts();
						if (conflicts.length > 0) {

							// Load the conflict error
							StringBuilder message = new StringBuilder(conflicts[0].getConflictDescription());
							for (int i = 1; i < conflicts.length; i++) {
								message.append("\n");
								message.append(conflicts[i].getConflictDescription());
							}
							this.labelConflict.set(message.toString());
							return;
						}

						// As here, no conflicts
						this.labelConflict.set("");

						// Set to new label before changing in model (stops loops)
						this.label.set(newValue);

						// Under take the change
						this.getChangeExecutor().execute(change);
					});
				}

				// Observe if label changed externally
				this.registerEventListener(this.getFactory().labelEvents, (event) -> {
					String newLabel = (String) event.getNewValue();
					this.label.set(newLabel);
				});
			}
		}

		@Override
		public ReadOnlyStringProperty getLabel() {
			return this.label == null ? null : this.label.getReadOnlyProperty();
		}

		@Override
		public StringProperty getEditLabel() {
			return this.label;
		}

		@Override
		public Property<String> getStylesheet() {
			return this.getFactory().stylesheetContent;
		}

		@Override
		public ReadOnlyProperty<URL> getStylesheetUrl() {
			return this.getFactory().stylesheetUrl;
		}

		@Override
		public List<ChildrenGroup<M, ?>> getChildrenGroups() {
			return this.childrenGroups;
		}

		@Override
		public Node createVisual(AdaptedChildVisualFactoryContext<M> context) {
			return this.getFactory().viewFactory.createVisual(this.getModel(), context);
		}

		@Override
		public int getDragLatency() {
			return this.getFactory().getContentPartFactory().getDragLatency();
		}

		@Override
		public SelectOnly getSelectOnly() {
			return this.getFactory().getContentPartFactory().getSelectOnly();
		}

		@Override
		protected void loadDescendantConnections(List<AdaptedConnection<?>> connections) {
			for (ChildrenGroup<M, ?> group : this.childrenGroups) {
				for (AdaptedChild<?> child : group.getChildren()) {
					connections.addAll(child.getConnections());
				}
			}
		}
	}

}
