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

package net.officefloor.gef.editor.internal.parts;

import org.eclipse.gef.mvc.fx.parts.IContentPart;

import net.officefloor.gef.editor.AdaptedConnectable;
import net.officefloor.gef.editor.AdaptedConnector;
import net.officefloor.gef.editor.internal.handlers.CreateAdaptedConnectionOnDragHandler;

/**
 * {@link IContentPart} for the {@link AdaptedConnectable}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdaptedConnectablePart {

	/**
	 * Obtains the {@link AdaptedConnector}.
	 * 
	 * @return {@link AdaptedConnector}.
	 */
	AdaptedConnector<?> getContent();

	/**
	 * Specifies this as the active {@link AdaptedConnectablePart} for the
	 * {@link CreateAdaptedConnectionOnDragHandler}.
	 * 
	 * @param isActive Indicates if active.
	 */
	void setActiveConnector(boolean isActive);

}
