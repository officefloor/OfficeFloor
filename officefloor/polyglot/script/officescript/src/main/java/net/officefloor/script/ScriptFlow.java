/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.script;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.internal.structure.Flow;

/**
 * Provides means for script to invoke {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ScriptFlow {

	/**
	 * Undertakes the {@link Flow}.
	 * 
	 * @param parameter Parameter. May be <code>null</code>.
	 * @param callback  {@link FlowCallback}.
	 */
	void doFlow(Object parameter, FlowCallback callback);

}