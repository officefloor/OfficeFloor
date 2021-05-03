/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal;

import javafx.beans.property.Property;
import net.officefloor.gef.configurer.Actioner;
import net.officefloor.gef.configurer.Builder;
import net.officefloor.gef.configurer.ErrorListener;

/**
 * Context for the {@link ValueRenderer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueRendererContext<M> {

	/**
	 * Obtains the model.
	 * 
	 * @return Model.
	 */
	M getModel();

	/**
	 * Triggers reloading the value for the provided {@link Builder}.
	 * 
	 * @param builder
	 *            {@link Builder} to identify the value to reload from the model.
	 */
	void reload(Builder<?, ?, ?> builder);

	/**
	 * Triggered to refresh the error display.
	 */
	void refreshError();

	/**
	 * Obtains the {@link Actioner}.
	 * 
	 * @return {@link Actioner} or <code>null</code> if not able to apply
	 *         configuration.
	 */
	Actioner getOptionalActioner();

	/**
	 * Obtains the dirty {@link Property}.
	 * 
	 * @return Dirty {@link Property}.
	 */
	Property<Boolean> dirtyProperty();

	/**
	 * Obtains the valid {@link Property}.
	 * 
	 * @return Valid {@link Property}.
	 */
	Property<Boolean> validProperty();

	/**
	 * Obtains the {@link ErrorListener}.
	 * 
	 * @return {@link ErrorListener}.
	 */
	ErrorListener getErrorListener();

}
