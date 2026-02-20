package net.officefloor.activity.compose.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfig;
import net.officefloor.activity.compose.FunctionConfig;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

        // Capture the initial procedure
        String serviceName = composeConfig.getService();
        ProcedureType serviceProcecureType = null;
        SubSection serviceProcedure = null;

        // Load the procedures
        for (String procedureName : composeConfig.getFunctions().keySet()) {
            FunctionConfig functionConfig = composeConfig.getFunctions().get(procedureName);

            // Obtain details of function
            String className = functionConfig.getClassName();

            // Determine the method name
            String methodName;
            Procedure[] procedureOptions = procedureLoader.listProcedures(className);
            if (procedureOptions != null && procedureOptions.length == 1) {
                // Use the only method configured
                methodName = procedureOptions[0].getProcedureName();
            } else {
                // Rely on configuration
                methodName = functionConfig.getMethod();
            }

            // Load the configuration
            PropertyList properties = new PropertyListImpl();

            // Load the procedure
            SubSection procedure = procedureArchitect.addProcedure(procedureName, className, "Class", methodName, false, properties);

            // Determine if initial procedure
            if (serviceName.equals(procedureName)) {
                // Initial procedure
                serviceProcedure = procedure;
                serviceProcecureType = procedureLoader.loadProcedureType(className, "Class", methodName, properties);
            }
        }

        // Obtain the initial servicing procedure
        Class<?> serviceParameterType =  serviceProcecureType.getParameterType();

        // Link input to first procedure
        SectionInput input = sectionDesigner.addSectionInput(ComposeArchitect.INPUT_NAME, serviceParameterType != null ? serviceParameterType.getName() : null);
        sectionDesigner.link(input, serviceProcedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
    }
}
