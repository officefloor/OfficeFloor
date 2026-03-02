package net.officefloor.activity.compose.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfig;
import net.officefloor.activity.compose.FunctionConfig;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureObjectType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;

import java.io.Reader;
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

        // Obtain the properties for configuration
        PropertyList configurationProperties = sectionSourceContext.createPropertyList();
        for (String propertyName : sectionSourceContext.getPropertyNames()) {
            configurationProperties.addProperty(propertyName).setValue(sectionSourceContext.getProperty(propertyName));
        }

        // Load the YAML composition
        Reader compositionConfiguration = sectionSourceContext
                .getConfigurationItem(sectionSourceContext.getSectionLocation(), configurationProperties)
                .getReader();
        ComposeConfig composeConfig = MAPPER.readValue(compositionConfiguration, ComposeConfig.class);

        // Create the procedure architect
        ProcedureLoader procedureLoader = ProcedureEmployer.employProcedureLoader(sectionDesigner, sectionSourceContext);
        ProcedureArchitect<SubSection> procedureArchitect = ProcedureEmployer.employProcedureDesigner(sectionDesigner, sectionSourceContext);

        // Capture the initial procedure
        String serviceName = composeConfig.getStart();
        ProcedureType serviceProcecureType = null;
        SubSection serviceProcedure = null;

        // Load the procedures
        Map<String, SubSection> procedures = new HashMap<>();
        Map<String, SectionObject> externalObjects = new HashMap<>();
        for (String procedureName : composeConfig.getFunctions().keySet()) {
            FunctionConfig functionConfig = composeConfig.getFunctions().get(procedureName);

            // Obtain details of function (ensuring class available)
            String className = functionConfig.getClassName();
            sectionSourceContext.loadClass(className);

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

            // Determine if next
            String next = functionConfig.getNext();
            boolean isNext = ((next != null) && (!next.isEmpty()));

            // Load the procedure
            SubSection procedure = procedureArchitect.addProcedure(procedureName, className, "Class", methodName, isNext, properties);
            procedures.put(procedureName, procedure);

            // Determine if initial procedure
            if (serviceName.equals(procedureName)) {
                // Initial procedure
                serviceProcedure = procedure;
                serviceProcecureType = procedureLoader.loadProcedureType(className, "Class", methodName, properties);
            }

            // Load the object dependencies
            ProcedureType procedureType = procedureLoader.loadProcedureType(className, "Class", methodName, properties);
            for (ProcedureObjectType procedureObjectType : procedureType.getObjectTypes()) {

                // Create object name (with focus of auto-wiring the object dependencies)
                String objectType = procedureObjectType.getObjectType().getName();
                String objectTypeQualifier = procedureObjectType.getTypeQualifier();
                String objectName = ((objectTypeQualifier != null) ? objectTypeQualifier + "_" : "") + objectType;

                // Obtain the external object
                SectionObject externalObject = externalObjects.computeIfAbsent(objectName, (key) -> {
                    SectionObject object = sectionDesigner.addSectionObject(objectName, objectType);
                    if (objectTypeQualifier != null) {
                        object.setTypeQualifier(objectTypeQualifier);
                    }
                    return object;
                });

                // Link object
                sectionDesigner.link(procedure.getSubSectionObject(procedureObjectType.getObjectName()), externalObject);
            }
        }

        // Map flow between the procedures
        for (String procedureName : composeConfig.getFunctions().keySet()) {
            FunctionConfig functionConfig = composeConfig.getFunctions().get(procedureName);

            // Obtain the procedure
            SubSection procedure = procedures.get(procedureName);

            // Determine if next
            String next = functionConfig.getNext();
            if ((next != null) && (!next.isEmpty())) {

                // Obtain the next procedure
                SubSection nextProcedure = procedures.get(next);

                // TODO handle no procedure

                // Map to procedure
                sectionDesigner.link(procedure.getSubSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME), nextProcedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
            }

            // Map flow outputs
            Map<String, String> outputs = functionConfig.getOutputs();
            if (outputs != null) {
                for (String outputName : outputs.keySet()) {

                    // Obtain the handling procedure
                    String handlingProcedureName = outputs.get(outputName);
                    SubSection handlingProcedure = procedures.get(handlingProcedureName);

                    // TODO handle no procedure

                    // Map to procedure
                    sectionDesigner.link(procedure.getSubSectionOutput(outputName), handlingProcedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
                }
            }
        }

        // Obtain the initial servicing procedure
        Class<?> serviceParameterType =  serviceProcecureType.getParameterType();

        // Link input to first procedure
        SectionInput input = sectionDesigner.addSectionInput(ComposeArchitect.INPUT_NAME, serviceParameterType != null ? serviceParameterType.getName() : null);
        sectionDesigner.link(input, serviceProcedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
    }
}
