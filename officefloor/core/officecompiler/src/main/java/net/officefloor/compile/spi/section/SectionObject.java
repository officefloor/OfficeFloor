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

package net.officefloor.compile.spi.section;

import net.officefloor.compile.internal.structure.SectionNode;

/**
 * Object required by the {@link SectionNode}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionObject extends SectionDependencyObjectNode {

	/**
	 * Obtains the name of this {@link SectionObject}.
	 * 
	 * @return Name of this {@link SectionObject}.
	 */
	String getSectionObjectName();

	/**
	 * Specifies the type qualifier.
	 * 
	 * @param qualifier Type qualifier.
	 */
	void setTypeQualifier(String qualifier);

	/**
	 * Adds an annotation.
	 * 
	 * @param annotation Annotation.
	 */
	void addAnnotation(Object annotation);

}
