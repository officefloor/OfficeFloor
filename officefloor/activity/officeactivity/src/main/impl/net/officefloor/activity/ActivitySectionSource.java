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

import net.officefloor.activity.model.ActivityModel;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;
import net.officefloor.configuration.ConfigurationItem;

/**
 * {@link SectionSource} to load the {@link ActivityModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class ActivitySectionSource extends AbstractSectionSource {

	/*
	 * =================== SectionSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void sourceSection(SectionDesigner designer, SectionSourceContext context) throws Exception {

		// Obtain helpers
		ProcedureArchitect<SubSection> procedureArchitect = ProcedureEmployer.employProcedureDesigner(designer,
				context);
		ProcedureLoader procedureLoader = ProcedureEmployer.employProcedureLoader(designer, context);

		// Load the activity
		new ActivityLoaderImpl().loadActivityConfiguration(new ActivityContext() {

			@Override
			public ConfigurationItem getConfiguration() {
				return context.getConfigurationItem(context.getSectionLocation(), null);
			}

			@Override
			public SectionDesigner getSectionDesigner() {
				return designer;
			}

			@Override
			public SectionSourceContext getSectionSourceContext() {
				return context;
			}

			@Override
			public ProcedureArchitect<SubSection> getProcedureArchitect() {
				return procedureArchitect;
			}

			@Override
			public ProcedureLoader getProcedureLoader() {
				return procedureLoader;
			}
		});
	}

}
