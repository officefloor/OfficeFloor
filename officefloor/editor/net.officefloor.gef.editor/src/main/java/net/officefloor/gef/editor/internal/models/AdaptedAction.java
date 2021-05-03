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
