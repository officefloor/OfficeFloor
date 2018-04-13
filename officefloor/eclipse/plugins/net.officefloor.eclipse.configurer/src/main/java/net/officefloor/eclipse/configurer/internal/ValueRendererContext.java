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
package net.officefloor.eclipse.configurer.internal;

import net.officefloor.eclipse.configurer.Actioner;
import net.officefloor.eclipse.configurer.ErrorListener;

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
	 * Obtains the {@link ErrorListener}.
	 * 
	 * @return {@link ErrorListener}.
	 */
	ErrorListener getErrorListener();

}