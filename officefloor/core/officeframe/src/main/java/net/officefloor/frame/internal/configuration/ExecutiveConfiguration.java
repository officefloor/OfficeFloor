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
package net.officefloor.frame.internal.configuration;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.source.SourceProperties;

/**
 * Configuration of an {@link Executive}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveConfiguration<XS extends ExecutiveSource> {

	/**
	 * Obtains the {@link ExecutiveSource} instance to use.
	 * 
	 * @return {@link ExecutiveSource} instance to use. This may be
	 *         <code>null</code> and therefore the
	 *         {@link #getExecutiveSourceClass()} should be used to obtain the
	 *         {@link ExecutiveSource}.
	 */
	XS getExecutiveSource();

	/**
	 * Obtains the {@link Class} of the {@link ExecutiveSource}.
	 * 
	 * @return {@link Class} of the {@link ExecutiveSource}.
	 */
	Class<XS> getExecutiveSourceClass();

	/**
	 * Obtains the {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 * 
	 * @return {@link SourceProperties} for initialising the {@link ExecutiveSource}.
	 */
	SourceProperties getProperties();

}
