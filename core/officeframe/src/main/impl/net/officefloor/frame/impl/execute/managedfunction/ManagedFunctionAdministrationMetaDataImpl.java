/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.managedfunction;

import java.util.logging.Logger;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.internal.structure.AdministrationMetaData;
import net.officefloor.frame.internal.structure.ManagedFunctionAdministrationMetaData;

/**
 * {@link ManagedFunctionAdministrationMetaData} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionAdministrationMetaDataImpl<E, F extends Enum<F>, G extends Enum<G>>
		implements ManagedFunctionAdministrationMetaData<E, F, G> {

	/**
	 * {@link Logger} for {@link AdministrationContext}.
	 */
	private final Logger logger;

	/**
	 * {@link AdministrationMetaData}.
	 */
	private final AdministrationMetaData<E, F, G> administrationMetaData;

	/**
	 * Instantiate.
	 * 
	 * @param logger                 {@link Logger} for
	 *                               {@link AdministrationContext}.
	 * @param administrationMetaData {@link AdministrationMetaData}.
	 */
	public ManagedFunctionAdministrationMetaDataImpl(Logger logger,
			AdministrationMetaData<E, F, G> administrationMetaData) {
		this.logger = logger;
		this.administrationMetaData = administrationMetaData;
	}

	/*
	 * =================== ManagedFunctionAdministrationMetaData ==================
	 */

	@Override
	public Logger getLogger() {
		return this.logger;
	}

	@Override
	public AdministrationMetaData<E, F, G> getAdministrationMetaData() {
		return this.administrationMetaData;
	}

}
