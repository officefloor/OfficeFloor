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

package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureProperty;
import net.officefloor.activity.procedure.spi.ProcedureSource;

/**
 * {@link Procedure} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureImpl implements Procedure {

	/**
	 * Procedure name.
	 */
	private final String procedureName;

	/**
	 * {@link ProcedureSource} name.
	 */
	private final String serviceName;

	/**
	 * {@link ProcedureProperty} instances.
	 */
	private final ProcedureProperty[] properties;

	/**
	 * Instantiate.
	 * 
	 * @param procedureName Procedure name.
	 * @param serviceName   {@link ProcedureSource} name.
	 * @param properties    {@link ProcedureProperty} instances.
	 */
	public ProcedureImpl(String procedureName, String serviceName, ProcedureProperty[] properties) {
		this.procedureName = procedureName;
		this.serviceName = serviceName;
		this.properties = properties;
	}

	/*
	 * =================== Procedure ======================
	 */

	@Override
	public String getProcedureName() {
		return this.procedureName;
	}

	@Override
	public String getServiceName() {
		return this.serviceName;
	}

	@Override
	public ProcedureProperty[] getProperties() {
		return this.properties;
	}

}
