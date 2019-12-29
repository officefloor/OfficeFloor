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
package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResourceStore;

/**
 * {@link ManagedFunction} to send the {@link HttpFile} from the
 * {@link HttpResourceStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class TriggerSendHttpFileFunction extends StaticManagedFunction<None, None> {

	/**
	 * {@link HttpPath}.
	 */
	private final HttpPath path;

	/**
	 * Instantiate.
	 * 
	 * @param path Path.
	 */
	public TriggerSendHttpFileFunction(String path) {
		this.path = new HttpPath(path);
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public void execute(ManagedFunctionContext<None, None> context) {
		// Trigger send file
		context.setNextFunctionArgument(this.path);
	}

}