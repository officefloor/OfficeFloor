/*-
 * #%L
 * Web Security
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

package net.officefloor.web.spi.security;

import net.officefloor.frame.api.function.FlowCallback;

/**
 * Generic context for integrating {@link HttpSecurity} actions into the
 * application.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityApplicationContext<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains a dependency.
	 * 
	 * @param key Key for the dependency.
	 * @return Dependency.
	 */
	Object getObject(O key);

	/**
	 * Undertakes a flow.
	 * 
	 * @param key       Key identifying the flow.
	 * @param parameter Parameter.
	 * @param callback  {@link FlowCallback}.
	 */
	void doFlow(F key, Object parameter, FlowCallback callback);

}
