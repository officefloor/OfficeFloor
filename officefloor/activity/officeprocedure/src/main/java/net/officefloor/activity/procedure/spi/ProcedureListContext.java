/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.activity.procedure.spi;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.compile.properties.Property;
import net.officefloor.frame.api.source.SourceContext;

/**
 * Context for listing the {@link Procedure} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcedureListContext {

	/**
	 * Obtains the resource to list the {@link Procedure} instances.
	 * 
	 * @return Resource to list the {@link Procedure} instances.
	 */
	String getResource();

	/**
	 * Adds an available {@link Procedure}.
	 * 
	 * @param procedureName Name of the {@link Procedure}.
	 * @return {@link ProcedureSpecification} to detail requirements for the
	 *         {@link Procedure}.
	 */
	ProcedureSpecification addProcedure(String procedureName);

	/**
	 * <p>
	 * Obtains the {@link SourceContext}.
	 * <p>
	 * Note that the {@link Property} values will not be available from this
	 * {@link SourceContext}. It is typically only provided to enable to load
	 * resource as a {@link Class}.
	 * 
	 * @return {@link SourceContext}.
	 */
	SourceContext getSourceContext();

}