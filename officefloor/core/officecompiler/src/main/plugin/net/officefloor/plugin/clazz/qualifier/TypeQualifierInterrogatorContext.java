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

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;

import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Context for the {@link TypeQualifierInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TypeQualifierInterrogatorContext extends StatePoint {

	/**
	 * <p>
	 * Obtains the {@link AnnotatedElement}.
	 * <p>
	 * Typically this is either a {@link Field} or {@link Parameter}.
	 * 
	 * @return {@link AnnotatedElement}.
	 */
	AnnotatedElement getAnnotatedElement();

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
