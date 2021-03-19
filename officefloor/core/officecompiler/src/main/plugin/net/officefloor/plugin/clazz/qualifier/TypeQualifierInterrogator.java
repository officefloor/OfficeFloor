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

package net.officefloor.plugin.clazz.qualifier;

/**
 * Interrogates for the type qualifier.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeQualifierInterrogator {

	/**
	 * Interrogates for the type qualifier.
	 * 
	 * @param context {@link TypeQualifierInterrogatorContext}.
	 * @return Type qualifier if can determine. Otherwise, <code>null</code> to
	 *         allow other {@link TypeQualifierInterrogator} to determine.
	 * @throws Exception If fails to interrogate.
	 */
	String interrogate(TypeQualifierInterrogatorContext context) throws Exception;

}
