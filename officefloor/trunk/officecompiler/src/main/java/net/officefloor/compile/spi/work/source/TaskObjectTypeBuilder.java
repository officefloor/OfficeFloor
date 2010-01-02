/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.officefloor.compile.spi.work.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.execute.Task;

/**
 * Provides means for the {@link WorkSource} to provide a
 * <code>type definition</code> of a dependency {@link Object} required by the
 * {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TaskObjectTypeBuilder<M extends Enum<M>> {

	/**
	 * Specifies the {@link Enum} for this {@link TaskObjectTypeBuilder}. This is
	 * required to be set if <code>M</code> is not {@link None} or
	 * {@link Indexed}.
	 * 
	 * @param key
	 *            {@link Enum} for this {@link TaskObjectTypeBuilder}.
	 */
	void setKey(M key);

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link Object}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link Object}. If not set the {@link TaskTypeBuilder} will use the following
	 * order to get a display label:
	 * <ol>
	 * <li>{@link Enum} key name</li>
	 * <li>index value</li>
	 * </ol>
	 * 
	 * @param label
	 *            Display label for the {@link Object}.
	 */
	void setLabel(String label);

}