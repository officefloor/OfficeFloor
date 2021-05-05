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

package net.officefloor.gef.configurer;

/**
 * Builder of a {@link Class} value.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassBuilder<M> extends Builder<M, String, ClassBuilder<M>> {

	/**
	 * Super type of the required {@link Class}.
	 * 
	 * @param superType
	 *            Super type of the {@link Class}.
	 * @return <code>this</code>.
	 */
	ClassBuilder<M> superType(Class<?> superType);

}
