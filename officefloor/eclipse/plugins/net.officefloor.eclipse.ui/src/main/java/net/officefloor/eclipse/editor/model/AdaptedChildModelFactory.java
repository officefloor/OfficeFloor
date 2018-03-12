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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedChildBuilder;
import net.officefloor.eclipse.editor.AdaptedModel;
import net.officefloor.eclipse.editor.AdaptedParentBuilder;
import net.officefloor.eclipse.editor.ChildGroupBuilder;
import net.officefloor.eclipse.editor.ViewFactory;
import net.officefloor.eclipse.editor.ViewFactoryContext;
import net.officefloor.eclipse.editor.model.ChildrenGroupFactory.ChildrenGroup;
import net.officefloor.eclipse.editor.parts.AdaptedChildPart;
import net.officefloor.eclipse.editor.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;
import net.officefloor.model.change.Change;
import net.officefloor.model.change.Conflict;

/**
 * Adapter for an output {@link Model}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedChildModelFactory<M extends Model, E extends Enum<E>, A extends AdaptedChild<M>>
		extends AbstractAdaptedModelFactory<M, E, A> implements AdaptedChildBuilder<M, E> {

	/**
	 * {@link ViewFactory}.
	 */
	private final ViewFactory<M, A> viewFactory;

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
	private final List<ChildrenGroupFactory<M, E>> childrenGroups = new ArrayList<>();

	/**
	 * Instantiate as {@link AdaptedChild}.
	 * 
	 * @param modelClass
	 *            {@link Model} {@link Class}.
	 * @param viewFactory
	 *            {@link ViewFactory}.
	 * @param parentAdaptedModel
	 *            Parent {@link AbstractAdaptedModelFactory}.
	 */
	@SuppressWarnings("unchecked")
	public AdaptedChildModelFactory(Class<M> modelClass, ViewFactory<M, A> viewFactory,
			AbstractAdaptedModelFactory<?, ?, ?> parentAdaptedModel) {
		super(modelClass, AdaptedChildPart.class, () -> (A) new AdaptedChildImpl<M, E, AdaptedChild<M>>(),
				parentAdaptedModel);
		this.viewFactory = viewFactory;
	}

	/**
	 * Allow {@link AdaptedParentBuilder} inheritance.
	 * 
	 * @param modelClass
	 *            {@link Model} {@link Class}.
	 * @param partClass
	 *            {@link IContentPart} {@link Class}.
	 * @param newAdaptedModel
	 *            {@link Supplier} for the {@link AdaptedModel}.
	 * @param viewFactory
	 *            {@link ViewFactory}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	protected AdaptedChildModelFactory(Class<M> modelClass, Class<?> partClass, Supplier<A> newAdaptedModel,
			ViewFactory<M, A> viewFactory, OfficeFloorContentPartFactory contentPartFactory) {
		super(modelClass, partClass, newAdaptedModel, contentPartFactory);
		this.viewFactory = viewFactory;
	}

	@Override
	public void validate(Map<Class<?>, AbstractAdaptedModelFactory<?, ?, ?>> models) throws IllegalStateException {

		// Construct the view (ensures all children groups are registered)
		M model;
		try {
			model = this.getModelClass().newInstance();
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Unable to instantiate with default constructor " + this.getModelClass().getName());
		}
		A adaptedModel = this.createAdaptedModel(model, null);
		adaptedModel.createVisual();
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
	public final ChildGroupBuilder children(String childGroupName, Function<M, List<? extends Model>> getChildren,
			E... childrenEvents) {
		ChildrenGroupFactory<M, E> factory = new ChildrenGroupFactory<>(childGroupName, getChildren, childrenEvents,
				this);
		this.childrenGroups.add(factory);
		return factory;
	}

	/**
	 * {@link AdaptedChild} implementation.
	 */
	protected static class AdaptedChildImpl<M extends Model, E extends Enum<E>, A extends AdaptedChild<M>>
			extends AbstractAdaptedModel<M, E, A, AdaptedChildModelFactory<M, E, A>>
			implements AdaptedChild<M>, ViewFactoryContext {

		/**
		 * {@link ChildrenGroup} instances.
		 */
		private List<ChildrenGroup<M, ?>> childrenGroups;

		/**
		 * Label for the {@link Model}.
		 */
		private ReadOnlyStringWrapper label;

		/**
		 * Potential conflict in {@link LabelChange}.
		 */
		private ReadOnlyStringWrapper labelConflict;

		/*
		 * =================== AdaptedChild =====================
		 */

		@Override
		protected void init() {

			// Load the children groups
			this.childrenGroups = new ArrayList<>(this.getFactory().childrenGroups.size());
			for (ChildrenGroupFactory<M, E> childrenGroupFactory : this.getFactory().childrenGroups) {
				this.childrenGroups.add(childrenGroupFactory.createChildrenGroup(this.getModel()));
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
		public List<ChildrenGroup<M, ?>> getChildrenGroups() {
			return this.childrenGroups;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Pane createVisual() {

			// Clear the panes
			for (ChildrenGroup<M, ?> childrenGroups : this.childrenGroups) {
				childrenGroups.setPane(null);
			}

			// Create the visual pane
			Pane pane = this.getFactory().viewFactory.createView((A) this, this);

			// Ensure all children groups are configured
			for (ChildrenGroup<M, ?> childrenGroup : this.childrenGroups) {
				if (childrenGroup.getPane() == null) {
					throw new IllegalStateException("Children group Pane '" + childrenGroup.getChildrenGroupName()
							+ "' not configured in view of model " + this.getFactory().getModelClass().getName());
				}
			}

			// Return the visual pane
			return pane;
		}

		/*
		 * ========================== ViewFactoryContext ==========================
		 */

		@Override
		public Label label(Pane parent) {
			Label label = this.addNode(parent, new Label());
			label.textProperty().bind(this.label.getReadOnlyProperty());
			return label;
		}

		@Override
		public <N extends Node> N addNode(Pane parent, N node) {
			parent.getChildren().add(node);
			return node;
		}

		@Override
		public void childGroup(String childGroupName, Pane parent) {
			if (childGroupName == null) {
				throw new NullPointerException(
						"No child group name provided for view of " + this.getFactory().getModelClass().getName());
			}

			// Load the child group pane
			for (ChildrenGroup<M, ?> childrenGroup : this.childrenGroups) {
				if (childGroupName.equals(childrenGroup.getChildrenGroupName())) {

					// Found the child group, so load the pane
					childrenGroup.setPane(parent);
					return;
				}
			}

			// As here, no children group registered
			throw new IllegalStateException("No children group '" + childGroupName + "' registered for view of model "
					+ this.getFactory().getModelClass().getName());
		}
	}

}