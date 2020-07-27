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

package net.officefloor.plugin.section.clazz.flow;

import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} link for {@link SubSectionOutput}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassSectionSubSectionOutputLink {

	/**
	 * Obtains the {@link SubSectionOutput} name.
	 * 
	 * @return {@link SubSectionOutput} name.
	 */
	String getSubSectionOutputName();

	/**
	 * Obtains the link name.
	 * 
	 * @return Link name.
	 */
	String getLinkName();

}
