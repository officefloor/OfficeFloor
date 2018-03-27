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
package net.officefloor.eclipse.editor.internal.models;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import net.officefloor.eclipse.editor.AdaptedChild;
import net.officefloor.eclipse.editor.AdaptedConnection;

/**
 * Indicates an active {@link AdaptedConnector} source for dragging creation of
 * an {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActiveConnectionSourceModel {

	/**
	 * Active {@link AdaptedChild}.
	 */
	private final ReadOnlyObjectWrapper<AdaptedChild<?>> activeAdaptedChild = new ReadOnlyObjectWrapper<>(null);

	/**
	 * Instantiate.
	 * 
	 * @param activeAdaptedChild
	 *            Active {@link AdaptedChild}.
	 */
	public void setActiveAdaptedChild(AdaptedChild<?> activeAdaptedChild) {
		this.activeAdaptedChild.set(activeAdaptedChild);
	}

	/**
	 * Allows listening for the active {@link AdaptedChild}.
	 * 
	 * @return {@link ReadOnlyObjectProperty} to listen for changes to the active
	 *         {@link AdaptedChild}.
	 */
	public ReadOnlyObjectProperty<AdaptedChild<?>> getActiveAdaptedChild() {
		return this.activeAdaptedChild.getReadOnlyProperty();
	}
}
