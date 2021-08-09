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
