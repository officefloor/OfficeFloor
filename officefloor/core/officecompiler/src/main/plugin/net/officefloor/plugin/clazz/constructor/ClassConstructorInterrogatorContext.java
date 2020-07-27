/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.constructor;

import java.lang.reflect.Constructor;

/**
 * Context for the {@link ClassConstructorInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogatorContext {

	/**
	 * Obtains the {@link Class} of object to be constructed for dependency
	 * injection.
	 * 
	 * @return {@link Class} of object to be constructed for dependency injection.
	 */
	Class<?> getObjectClass();

	/**
	 * <p>
	 * Provides optional error information about why a {@link Constructor} was not
	 * found.
	 * <p>
	 * This provides improved feedback to help resolve issue of why a
	 * {@link Constructor} was not selected in interrogation.
	 * 
	 * @param errorInformation Error information.
	 */
	void setErrorInformation(String errorInformation);

}
