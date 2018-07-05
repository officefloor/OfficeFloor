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
package net.officefloor.compile.spi.managedfunction.source;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;

/**
 * Provides means for the {@link ManagedFunctionSource} to provide a
 * <code>type definition</code> of a dependency {@link Object} required by the
 * {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionObjectTypeBuilder<M extends Enum<M>> {

	/**
	 * Specifies the {@link Enum} for this
	 * {@link ManagedFunctionObjectTypeBuilder}. This is required to be set if
	 * <code>M</code> is not {@link None} or {@link Indexed}.
	 * 
	 * @param key
	 *            {@link Enum} for this
	 *            {@link ManagedFunctionObjectTypeBuilder}.
	 */
	void setKey(M key);

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier
	 *            Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

	/**
	 * <p>
	 * Provides means to specify a display label for the {@link Object}.
	 * <p>
	 * This need not be set as is only an aid to better identify the
	 * {@link Object}. If not set the {@link ManagedFunctionTypeBuilder} will
	 * use the following order to get a display label:
	 * <ol>
	 * <li>{@link Enum} key name</li>
	 * <li>index value</li>
	 * </ol>
	 * 
	 * @param label
	 *            Display label for the {@link Object}.
	 */
	void setLabel(String label);

	/**
	 * Adds an annotation.
	 * 
	 * @param annotation
	 *            Annotation.
	 */
	void addAnnotation(Object annotation);

}