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

import javafx.scene.Node;
import net.officefloor.gef.editor.AdaptedActionVisualFactory;
import net.officefloor.gef.editor.AdaptedActionVisualFactoryContext;
import net.officefloor.gef.editor.AdaptedErrorHandler;
import net.officefloor.gef.editor.ModelAction;
import net.officefloor.gef.editor.ModelActionContext;
import net.officefloor.model.Model;

/**
 * Adapted {@link ModelAction}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedAction<R extends Model, O, M extends Model> {

	/**
	 * {@link ModelAction}.
	 */
	private final ModelAction<R, O, M> action;

	/**
	 * {@link ModelActionContext}.
	 */
	private final ModelActionContext<R, O, M> actionContext;

	/**
	 * {@link AdaptedActionVisualFactory}.
	 */
	private final AdaptedActionVisualFactory visualFactory;

	/**
	 * {@link AdaptedErrorHandler}.
	 */
	private final AdaptedErrorHandler errorHandler;

	/**
	 * Instantiate.
	 * 
	 * @param action
	 *            {@link ModelAction}.
	 * @param actionContext
	 *            {@link ModelActionContext}.
	 * @param visualFactory
	 *            {@link AdaptedActionVisualFactory}.
	 * @param errorHandler
	 *            {@link AdaptedErrorHandler}.
	 */
	public AdaptedAction(ModelAction<R, O, M> action, ModelActionContext<R, O, M> actionContext,
			AdaptedActionVisualFactory visualFactory, AdaptedErrorHandler errorHandler) {
		this.action = action;
		this.actionContext = actionContext;
		this.visualFactory = visualFactory;
		this.errorHandler = errorHandler;
	}

	/**
	 * Executes the {@link AdaptedAction}.
	 */
	public void execute() {
		this.errorHandler.isError(() -> this.action.execute(this.actionContext));
	}

	/**
	 * Creates the visual.
	 * 
	 * @param context
	 *            {@link AdaptedActionVisualFactoryContext}.
	 * @return Visual.
	 */
	public Node createVisual(AdaptedActionVisualFactoryContext context) {
		return this.visualFactory.createVisual(context);
	}

}
