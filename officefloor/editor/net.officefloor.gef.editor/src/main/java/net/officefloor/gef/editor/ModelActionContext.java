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

package net.officefloor.gef.editor;

import com.google.inject.Injector;

import net.officefloor.model.Model;

/**
 * Context for the {@link ModelAction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ModelActionContext<R extends Model, O, M extends Model> {

	/**
	 * Obtains the root {@link Model}.
	 * 
	 * @return Root {@link Model}.
	 */
	R getRootModel();

	/**
	 * Obtains the operations.
	 * 
	 * @return Operations.
	 */
	O getOperations();

	/**
	 * <p>
	 * Obtains the {@link Model}.
	 * <p>
	 * Will only be <code>null</code> if action is to create a {@link Model}.
	 * 
	 * @return {@link Model} or <code>null</code>.
	 */
	M getModel();

	/**
	 * <p>
	 * Obtains the {@link AdaptedModel}.
	 * <p>
	 * Will only be <code>null</code> if action is to create a {@link Model}.
	 * 
	 * @return {@link AdaptedModel} or <code>null</code>.
	 */
	AdaptedModel<M> getAdaptedModel();

	/**
	 * Shows an overlay at the position of the action.
	 * 
	 * @param overlayVisualFactory
	 *            {@link OverlayVisualFactory}.
	 */
	void overlay(OverlayVisualFactory overlayVisualFactory);

	/**
	 * Obtains the {@link ChangeExecutor}.
	 * 
	 * @return {@link ChangeExecutor}.
	 */
	ChangeExecutor getChangeExecutor();

	/**
	 * Obtains the {@link Injector}.
	 * 
	 * @return {@link Injector}.
	 */
	Injector getInjector();

	/**
	 * Convenience method to position the {@link Model}.
	 * 
	 * @param model
	 *            {@link Model} to be positioned.
	 * @return Input {@link Model}.
	 */
	M position(M model);

}
