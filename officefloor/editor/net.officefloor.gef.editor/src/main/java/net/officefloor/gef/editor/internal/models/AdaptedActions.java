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

import java.util.List;

import net.officefloor.gef.editor.ModelAction;
import net.officefloor.model.Model;

/**
 * Adapted {@link ModelAction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedActions<R extends Model, O, M extends Model> {

	/**
	 * {@link AdaptedAction} instances.
	 */
	private final List<AdaptedAction<R, O, M>> actions;

	/**
	 * Instantiate.
	 * 
	 * @param actions
	 *            {@link AdaptedAction} instances.
	 */
	public AdaptedActions(List<AdaptedAction<R, O, M>> actions) {
		this.actions = actions;
	}

	/**
	 * Obtains the {@link AdaptedAction} instances.
	 * 
	 * @return {@link AdaptedAction} instances.
	 */
	public List<AdaptedAction<R, O, M>> getAdaptedActions() {
		return this.actions;
	}

}
