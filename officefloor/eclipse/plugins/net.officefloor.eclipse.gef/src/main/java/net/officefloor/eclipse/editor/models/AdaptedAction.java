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

import javafx.scene.Node;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactory;
import net.officefloor.eclipse.editor.AdaptedActionVisualFactoryContext;
import net.officefloor.eclipse.editor.AdaptedParent;
import net.officefloor.eclipse.editor.ModelAction;
import net.officefloor.eclipse.editor.ModelActionContext;
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
	private final ModelAction<R, O, M, AdaptedParent<M>> action;

	/**
	 * {@link ModelActionContext}.
	 */
	private final ModelActionContext<R, O, M, AdaptedParent<M>> actionContext;

	/**
	 * {@link AdaptedActionVisualFactory}.
	 */
	private final AdaptedActionVisualFactory visualFactory;

	/**
	 * Instantiate.
	 * 
	 * @param action
	 *            {@link ModelAction}.
	 * @param actionContext
	 *            {@link ModelActionContext}.
	 * @param visualFactory
	 *            {@link AdaptedActionVisualFactory}.
	 */
	public AdaptedAction(ModelAction<R, O, M, AdaptedParent<M>> action,
			ModelActionContext<R, O, M, AdaptedParent<M>> actionContext, AdaptedActionVisualFactory visualFactory) {
		this.action = action;
		this.actionContext = actionContext;
		this.visualFactory = visualFactory;
	}

	/**
	 * Executes the {@link AdaptedAction}.
	 */
	public void execute() {
		this.action.execute(this.actionContext);
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