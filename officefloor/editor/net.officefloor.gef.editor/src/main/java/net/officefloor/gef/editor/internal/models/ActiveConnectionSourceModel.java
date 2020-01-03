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