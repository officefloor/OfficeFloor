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
import net.officefloor.eclipse.editor.AdaptedConnectorRole;

/**
 * Indicates an active {@link AdaptedConnector} for dragging creation of an
 * {@link AdaptedConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActiveConnectionSourceModel {

	/**
	 * Active {@link ActiveConnectionSource}.
	 */
	private final ReadOnlyObjectWrapper<ActiveConnectionSource> activeSource = new ReadOnlyObjectWrapper<>(null);

	/**
	 * Instantiate.
	 * 
	 * @param activeAdaptedChild
	 *            Active {@link AdaptedChild}.
	 * @param role
	 *            {@link AdaptedConnectorRole}.
	 */
	public void setActiveSource(AdaptedChild<?> activeAdaptedChild, AdaptedConnectorRole role) {
		if (activeAdaptedChild == null) {
			this.activeSource.set(null);
		} else {
			this.activeSource.set(new ActiveConnectionSource(activeAdaptedChild, role));
		}
	}

	/**
	 * Allows listening for the active {@link ActiveConnectionSource}.
	 * 
	 * @return {@link ReadOnlyObjectProperty} to listen for changes to the active
	 *         {@link ActiveConnectionSource}.
	 */
	public ReadOnlyObjectProperty<ActiveConnectionSource> activeSource() {
		return this.activeSource.getReadOnlyProperty();
	}

	/**
	 * Active connection source.
	 */
	public static class ActiveConnectionSource {

		/**
		 * Source {@link AdaptedChild}.
		 */
		private final AdaptedChild<?> child;

		/**
		 * Role of source {@link AdaptedChild}.
		 */
		private final AdaptedConnectorRole role;

		/**
		 * Instantiate.
		 * 
		 * @param child
		 *            Source {@link AdaptedChild}.
		 * @param role
		 *            Role of source {@link AdaptedChild}.
		 */
		private ActiveConnectionSource(AdaptedChild<?> child, AdaptedConnectorRole role) {
			this.child = child;
			this.role = role;
		}

		/**
		 * Obtains the source {@link AdaptedChild}.
		 * 
		 * @return Source {@link AdaptedChild}.
		 */
		public AdaptedChild<?> getSource() {
			return this.child;
		}

		/**
		 * Obtains the role of the source {@link AdaptedChild}.
		 * 
		 * @return {@link AdaptedConnectorRole}. May be <code>null</code> if fulfills
		 *         any {@link AdaptedConnectorRole}.
		 */
		public AdaptedConnectorRole getRole() {
			return this.role;
		}
	}

}