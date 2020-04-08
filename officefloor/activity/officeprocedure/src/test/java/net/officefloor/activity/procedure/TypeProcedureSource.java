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

package net.officefloor.activity.procedure;

import java.lang.reflect.Method;

import net.officefloor.activity.procedure.spi.ProcedureListContext;
import net.officefloor.activity.procedure.spi.ProcedureMethodContext;
import net.officefloor.activity.procedure.spi.ProcedureSource;
import net.officefloor.activity.procedure.spi.ProcedureSourceServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link ProcedureSource} to obtain type indication.
 * 
 * @author Daniel Sagenschneider
 */
public class TypeProcedureSource implements ProcedureSource, ProcedureSourceServiceFactory {

	/**
	 * Indicates if type.
	 */
	public static Boolean isType = null;

	/**
	 * Source name.
	 */
	public static final String SOURCE_NAME = "Type";

	/**
	 * {@link Method} for {@link Procedure}.
	 */
	public void method() {
		// Should not be invoked
	}

	/*
	 * ==================== ProcedureSourceServiceFactory ==========================
	 */

	@Override
	public ProcedureSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================= ProcedureSource ===================================
	 */

	@Override
	public String getSourceName() {
		return SOURCE_NAME;
	}

	@Override
	public void listProcedures(ProcedureListContext context) throws Exception {
		// Should not be for listing
	}

	@Override
	public Method loadMethod(ProcedureMethodContext context) throws Exception {

		// Indicate if type
		isType = context.getSourceContext().isLoadingType();

		// Return method
		Class<?> clazz = context.getSourceContext().loadClass(context.getResource());
		return clazz.getMethod(context.getProcedureName());
	}

}
