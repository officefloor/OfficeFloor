/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.section;

import java.lang.reflect.Field;

import net.officefloor.plugin.managedobject.clazz.AbstractDependencyMetaData;
import net.officefloor.web.HttpSessionStateful;
import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.object.HttpSessionObjectManagedObject.Dependencies;

/**
 * {@link AbstractDependencyMetaData} for {@link HttpSessionStateful}.
 * 
 * @author Daniel Sagenschneider
 */
public class StatefulDependencyMetaData extends AbstractDependencyMetaData {

	/**
	 * {@link HttpSession} for providing {@link Field}.
	 */
	public HttpSession httpSession;

	/**
	 * Initiate.
	 * 
	 * @throws Exception
	 *             Should not occur but required for compiling.
	 */
	public StatefulDependencyMetaData() throws Exception {
		super(Dependencies.HTTP_SESSION.name(), Dependencies.HTTP_SESSION
				.ordinal(), StatefulDependencyMetaData.class
				.getField("httpSession"));
	}

}
