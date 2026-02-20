package net.officefloor.activity.compose.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfig;
import net.officefloor.activity.compose.FunctionConfig;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;

import java.io.InputStream;

/**
 * {@link net.officefloor.compile.spi.section.source.SectionSource} for
 */
public class ComposeSectionSource extends AbstractSectionSource {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /*
     * ==================== SectionSource ===========================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {

    }

    @Override
    public void sourceSection(SectionDesigner sectionDesigner, SectionSourceContext sectionSourceContext) throws Exception {

        // Load the YAML composition
        InputStream compositionConfiguration = sectionSourceContext.getResource(sectionSourceContext.getSectionLocation());
        ComposeConfig composeConfig = MAPPER.readValue(compositionConfiguration, ComposeConfig.class);

        // Create the procedure architect
        ProcedureLoader procedureLoader = ProcedureEmployer.employProcedureLoader(sectionDesigner, sectionSourceContext);
        ProcedureArchitect<SubSection> procedureArchitect = ProcedureEmployer.employProcedureDesigner(sectionDesigner, sectionSourceContext);

        // Load the procedures
        for (String procedureName : composeConfig.getFunctions().keySet()) {
            FunctionConfig functionConfig = composeConfig.getFunctions().get(procedureName);

            // Obtain details of function
            String className = functionConfig.getClassName();

            // Determine the method name
            String methodName;
            Procedure[] procedures = procedureLoader.listProcedures(className);
            if (procedures != null && procedures.length == 1) {
                // Use the only method configured
                methodName = procedures[0].getProcedureName();
            } else {
                // Rely on configuration
                methodName = functionConfig.getMethod();
            }

            // Load the procedure
            SubSection procedure = procedureArchitect.addProcedure(procedureName, functionConfig.getClassName(), "Class", methodName, false, new PropertyListImpl());
        }
    }
}
