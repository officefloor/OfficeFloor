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

import net.officefloor.eclipse.editor.AdaptedConnection;
import net.officefloor.eclipse.editor.AdaptedConnectionBuilder;
import net.officefloor.eclipse.editor.parts.AdaptedConnectionPart;
import net.officefloor.eclipse.editor.parts.OfficeFloorContentPartFactory;
import net.officefloor.model.ConnectionModel;

/**
 * Factory for an {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdaptedConnectionModelFactory<C extends ConnectionModel, E extends Enum<E>>
		extends AbstractAdaptedModelFactory<C, E, AdaptedConnection<C>> implements AdaptedConnectionBuilder<C, E> {

	/**
	 * Instantiate.
	 * 
	 * @param modelClass
	 *            {@link ConnectionModel} {@link Class}.
	 * @param contentPartFactory
	 *            {@link OfficeFloorContentPartFactory}.
	 */
	public AdaptedConnectionModelFactory(Class<C> modelClass, OfficeFloorContentPartFactory contentPartFactory) {
		super(modelClass, AdaptedConnectionPart.class, () -> new AdaptedConnectionImpl<>(), contentPartFactory);
	}

	/**
	 * {@link AdaptedConnection} implementation.
	 */
	public static class AdaptedConnectionImpl<C extends ConnectionModel, E extends Enum<E>>
			extends AbstractAdaptedModel<C, E, AdaptedConnection<C>, AdaptedConnectionModelFactory<C, E>>
			implements AdaptedConnection<C> {

		@Override
		protected void init() {
		}
	}

}