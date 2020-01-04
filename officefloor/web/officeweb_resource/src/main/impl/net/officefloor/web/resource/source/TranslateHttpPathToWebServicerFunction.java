/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource.source;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.web.route.WebServicer;

/**
 * Translates the {@link HttpPath} to {@link WebServicer}.
 * 
 * @author Daniel Sagenschneider
 */
public class TranslateHttpPathToWebServicerFunction
		extends StaticManagedFunction<TranslateHttpPathToWebServicerFunction.Dependencies, None> {

	/**
	 * Dependency keys.
	 */
	public static enum Dependencies {
		HTTP_PATH
	}

	/*
	 * ==================== ManagedFunction ==================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {
		HttpPath path = (HttpPath) context.getObject(Dependencies.HTTP_PATH);
		context.setNextFunctionArgument(path.getWebServicer());
	}

}
