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

package net.officefloor.gef.editor.internal.models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.gef.editor.AdaptedActionVisualFactory;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.gef.editor.ModelActionContext;
import net.officefloor.gef.editor.internal.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.Model;

/**
 * Factory for the creation of the {@link AdaptedActions}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedActionsFactory<R extends Model, O, M extends Model> {

	/**
	 * {@link OfficeFloorContentPartFactory}.
	 */
	private final OfficeFloorContentPartFactory<R, O> contentPartFactory;

	/**
	 * {@link ModelToAction} instances.
	 */
	private final List<ModelToAction<R, O, M>> modelToActions = new LinkedList<>();

	/**
	 * Instantiate.
	 * 
	 * @param contentPartFactory {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedActionsFactory(OfficeFloorContentPartFactory<R, O> contentPartFactory) {
		this.contentPartFactory = contentPartFactory;
	}

	/**
	 * Adds an {@link AdaptedAction}.
	 * 
	 * @param action        {@link ModelAction}.
	 * @param visualFactory {@link AdaptedActionVisualFactory}.
	 */
	public void addAction(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
		this.modelToActions.add(new ModelToAction<>(action, visualFactory));
	}

	/**
	 * Creates the {@link AdaptedActions}.
	 * 
	 * @param actionContext {@link ModelActionContext}.
	 * @return {@link AdaptedActions}.
	 */
	public AdaptedActions<R, O, M> createAdaptedActions(ModelActionContext<R, O, M> actionContext) {

		// Determine if actions
		if (this.modelToActions.size() == 0) {
			return null; // no actions
		}

		// Determine if click only
		boolean isClickOnly = (this.contentPartFactory.getSelectOnly() != null);

		// Load the model actions
		List<AdaptedAction<R, O, M>> actions = new ArrayList<>(this.modelToActions.size());
		for (ModelToAction<R, O, M> action : this.modelToActions) {

			// Obtain the action
			ModelAction<R, O, M> modelAction = action.action;
			if (isClickOnly) {
				// Click only, so dummy action
				modelAction = (context) -> {
				};
			}

			// Add the action
			actions.add(new AdaptedAction<>(modelAction, actionContext, action.visualFactory,
					this.contentPartFactory.getErrorHandler()));
		}
		return new AdaptedActions<>(actions);
	}

	/**
	 * {@link Model} to {@link ModelAction}.
	 */
	private static class ModelToAction<R extends Model, O, M extends Model> {

		/**
		 * {@link ModelAction}.
		 */
		private final ModelAction<R, O, M> action;

		/**
		 * {@link AdaptedActionVisualFactory}.
		 */
		private final AdaptedActionVisualFactory visualFactory;

		/**
		 * Instantiate.
		 * 
		 * @param action        {@link ModelAction}.
		 * @param visualFactory {@link AdaptedActionVisualFactory}.
		 */
		private ModelToAction(ModelAction<R, O, M> action, AdaptedActionVisualFactory visualFactory) {
			this.action = action;
			this.visualFactory = visualFactory;
		}
	}

}
