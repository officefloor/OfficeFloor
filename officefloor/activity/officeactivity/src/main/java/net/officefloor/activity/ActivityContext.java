/*-
 * #%L
 * Activity
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

package net.officefloor.activity;

import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.configuration.ConfigurationItem;

/**
 * Context for the {@link ActivityLoader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ActivityContext {

	/**
	 * Obtains the {@link ConfigurationItem} containing the configuration.
	 * 
	 * @return {@link ConfigurationItem} containing the configuration.
	 */
	ConfigurationItem getConfiguration();

	/**
	 * Obtains the {@link ProcedureArchitect}.
	 * 
	 * @return {@link ProcedureArchitect}.
	 */
	ProcedureArchitect<SubSection> getProcedureArchitect();

	/**
	 * Obtains the {@link ProcedureLoader}.
	 * 
	 * @return {@link ProcedureLoader}.
	 */
	ProcedureLoader getProcedureLoader();

	/**
	 * Obtains the {@link SectionDesigner}.
	 * 
	 * @return {@link SectionDesigner}.
	 */
	SectionDesigner getSectionDesigner();

	/**
	 * Obtains the {@link SectionSourceContext}.
	 * 
	 * @return {@link SectionSourceContext}.
	 */
	SectionSourceContext getSectionSourceContext();

}
