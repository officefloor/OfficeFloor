/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.spi.administration.source;

import net.officefloor.frame.api.administration.Administration;

/**
 * <p>
 * Source to obtain a particular type of {@link Administration}.
 * <p>
 * Implemented by the {@link Administration} provider.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministrationSource<E, F extends Enum<F>, G extends Enum<G>> {

	/**
	 * <p>
	 * Obtains the specification for this.
	 * <p>
	 * This will be called before any other methods, therefore this method must
	 * be able to return the specification immediately after a default
	 * constructor instantiation.
	 * 
	 * @return Specification of this.
	 */
	AdministrationSourceSpecification getSpecification();

	/**
	 * Initialises the {@link AdministrationSource}.
	 * 
	 * @param context
	 *            {@link AdministrationSourceContext} to initialise this
	 *            instance of the {@link AdministrationSource}.
	 * @return Meta-data to describe this.
	 * @throws Exception
	 *             Should the {@link AdministrationSource} fail to configure
	 *             itself from the input properties.
	 */
	AdministrationSourceMetaData<E, F, G> init(AdministrationSourceContext context) throws Exception;

}
