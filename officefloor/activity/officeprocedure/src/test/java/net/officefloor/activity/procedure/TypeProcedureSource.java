/*-
 * #%L
 * Procedure
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		Class<?> clazz = context.getResource();
		return clazz.getMethod(context.getProcedureName());
	}

}
