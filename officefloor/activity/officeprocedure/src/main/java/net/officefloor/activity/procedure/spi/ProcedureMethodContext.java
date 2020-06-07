/*-
 * #%L
 * Procedure
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

package net.officefloor.activity.procedure.spi;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.method.MethodObjectInstanceFactory;

/**
 * Context for the {@link ProcedureSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureMethodContext {

	/**
	 * Obtains the resource configured to provide the {@link Procedure}.
	 * 
	 * @return Resource configured to provide the {@link Procedure}.
	 */
	String getResource();

	/**
	 * Name of the {@link Procedure}.
	 * 
	 * @return Name of the {@link Procedure}.
	 */
	String getProcedureName();

	/**
	 * <p>
	 * Overrides the default {@link MethodObjectInstanceFactory}.
	 * <p>
	 * Specifying <code>null</code> indicates a static {@link Method}.
	 * 
	 * @param factory {@link MethodObjectInstanceFactory}.
	 */
	void setMethodObjectInstanceFactory(MethodObjectInstanceFactory factory);

	/**
	 * Obtains the {@link SourceContext}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}
