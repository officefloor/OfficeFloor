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