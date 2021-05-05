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

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnection;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.AdaptedConnectorRole;

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
	 * @param activeAdaptedConnectable Active {@link AdaptedConnectable}.
	 * @param role                     {@link AdaptedConnectorRole}.
	 */
	public void setActiveSource(AdaptedConnectable<?> activeAdaptedConnectable, AdaptedConnectorRole role) {
		if (activeAdaptedConnectable == null) {
			this.activeSource.set(null);
		} else {
			this.activeSource.set(new ActiveConnectionSource(activeAdaptedConnectable, role));
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
		 * Source {@link AdaptedConnectable}.
		 */
		private final AdaptedConnectable<?> connectable;

		/**
		 * Role of source {@link AdaptedConnectable}.
		 */
		private final AdaptedConnectorRole role;

		/**
		 * Instantiate.
		 * 
		 * @param connectable Source {@link AdaptedConnectable}.
		 * @param role        Role of source {@link AdaptedConnectable}.
		 */
		private ActiveConnectionSource(AdaptedConnectable<?> connectable, AdaptedConnectorRole role) {
			this.connectable = connectable;
			this.role = role;
		}

		/**
		 * Obtains the source {@link AdaptedConnectable}.
		 * 
		 * @return Source {@link AdaptedConnectable}.
		 */
		public AdaptedConnectable<?> getSource() {
			return this.connectable;
		}

		/**
		 * Obtains the role of the source {@link AdaptedConnectable}.
		 * 
		 * @return {@link AdaptedConnectorRole}. May be <code>null</code> if fulfills
		 *         any {@link AdaptedConnectorRole}.
		 */
		public AdaptedConnectorRole getRole() {
			return this.role;
		}
	}

}
