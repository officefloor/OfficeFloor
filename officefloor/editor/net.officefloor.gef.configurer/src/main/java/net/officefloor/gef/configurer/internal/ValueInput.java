/*-
 * #%L
 * [bundle] OfficeFloor Configurer
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

package net.officefloor.gef.configurer.internal;

import javafx.scene.Node;
import javafx.scene.Scene;
import net.officefloor.gef.configurer.Builder;

/**
 * Value input.
 * 
 * @author Daniel Sagenschneider
 */
public interface ValueInput {

	/**
	 * Obtains the {@link Node} for the input.
	 * 
	 * @return {@link Node} for the input.
	 */
	Node getNode();

	/**
	 * <p>
	 * Invoked once {@link Node} is connected to the {@link Scene}.
	 * <p>
	 * This allows {@link Scene} based activation of the {@link Node} (e.g.
	 * configuring a style sheet).
	 */
	default void activate() {
	}

	/**
	 * Invoked on reload of {@link Builder}. This allows hooking in to changing view
	 * on reload.
	 */
	default void reload() {
	}

}
