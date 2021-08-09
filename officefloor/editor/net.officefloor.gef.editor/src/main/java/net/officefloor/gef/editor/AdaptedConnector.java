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

package net.officefloor.gef.editor;

import java.util.List;

import javafx.scene.Node;
import net.officefloor.gef.editor.internal.parts.AdaptedConnectionPart;
import net.officefloor.gef.editor.internal.parts.AdaptedConnectorPart;
import net.officefloor.model.ConnectionModel;
import net.officefloor.model.Model;

/**
 * Adapted connector.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnector<M extends Model> {

	/**
	 * Obtains the parent {@link AdaptedConnectable}.
	 * 
	 * @return Parent {@link AdaptedConnectable}.
	 */
	AdaptedConnectable<M> getParentAdaptedConnectable();

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
	 * @param associatedAdaptedConnectors Associated {@link AdaptedConnector}
	 *                                    instances.
	 * @param associatedRole              {@link AdaptedConnectorRole}.
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
