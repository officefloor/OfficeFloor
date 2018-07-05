/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.eclipse.editor;

import java.util.List;

import javafx.scene.Node;
import net.officefloor.eclipse.editor.internal.parts.AdaptedConnectionPart;
import net.officefloor.eclipse.editor.internal.parts.AdaptedConnectorPart;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted connector.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnector<M extends Model> {

	/**
	 * Obtains the parent {@link AdaptedChild}.
	 * 
	 * @return Parent {@link AdaptedChild}.
	 */
	AdaptedChild<M> getParentAdaptedChild();

	/**
	 * Obtains the {@link ConnectionModel} {@link Class}.
	 * 
	 * @return {@link ConnectionModel} {@link Class}.
	 */
	Class<? extends ConnectionModel> getConnectionModelClass();

	/**
	 * <p>
	 * Specifies the associated {@link AdaptedConnector} instances.
	 * <p>
	 * Visually multiple {@link AdaptedConnectionPart} instances may be connected
	 * via the single {@link AdaptedConnectorPart} {@link Node}.
	 * 
	 * @param associatedAdaptedConnectors
	 *            Associated {@link AdaptedConnector} instances.
	 * @param associatedRole
	 *            {@link AdaptedConnectorRole}.
	 */
	void setAssociation(List<AdaptedConnector<M>> associatedAdaptedConnectors, AdaptedConnectorRole associatedRole);

	/**
	 * Indicates if able to create an {@link AdaptedConnection} from the association
	 * of {@link AdaptedConnector} instances.
	 * 
	 * @return <code>true</code> if able to create {@link AdaptedConnection}.
	 */
	boolean isAssociationCreateConnection();

	/**
	 * Obtains the role of this {@link AdaptedConnector}.
	 * 
	 * @return {@link AdaptedConnectorRole}. May be <code>null</code> to indicate
	 *         fills all roles.
	 */
	AdaptedConnectorRole getAssociationRole();

}