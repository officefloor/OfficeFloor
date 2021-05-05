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
