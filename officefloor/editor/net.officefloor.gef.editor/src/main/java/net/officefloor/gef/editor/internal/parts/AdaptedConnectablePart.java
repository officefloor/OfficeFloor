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
