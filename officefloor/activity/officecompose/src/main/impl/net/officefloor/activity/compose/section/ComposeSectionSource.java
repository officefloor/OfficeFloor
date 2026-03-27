package net.officefloor.activity.compose.section;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfig;
import net.officefloor.activity.compose.CompositionConfig;
import net.officefloor.activity.compose.FunctionConfig;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureEscalationType;
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
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.spi.section.source.impl.AbstractSectionSource;

import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        // No specification
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

                // Ensure have method name
                if (methodName == null) {
                    throw sectionDesigner.addIssue("Require configuring method for " + procedureName
                            + " (" + className + ") as it contains multiple public methods ("
                            + Arrays.stream(procedureOptions).map((procedure) -> procedure.getProcedureName()).collect(Collectors.joining(", ")) + ")");
                }
            }

            // Load the configuration
            PropertyList properties = new PropertyListImpl();

            // Determine if next
            String next = functionConfig.getNext();
            boolean isNext = ((next != null) && (!next.isEmpty()));

            // Load the procedure
            SubSection procedure = procedureArchitect.addProcedure(procedureName, className, "Class", methodName, isNext, properties);
            procedures.put(procedureName, procedure);

            // Load the procedure type
            ProcedureType procedureType = procedureLoader.loadProcedureType(className, "Class", methodName, properties);

            // Determine if initial procedure
            if (serviceName.equals(procedureName)) {
                // Initial procedure
                serviceProcedure = procedure;
                serviceProcecureType = procedureType;
            }

            // Load the object dependencies
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

            // Map escalations
            Map<Class<?>, SectionOutput> sectionEscalations = new HashMap<>();
            for (ProcedureEscalationType procedureEscalationType : procedureType.getEscalationTypes()) {

                // Obtain the escalation type
                Class<?> escalationType = procedureEscalationType.getEscalationType();
                String escalationTypeName = escalationType.getName();

                // Obtain the external escalation
                SectionOutput escalation = sectionEscalations.computeIfAbsent(escalationType, (key) -> {
                    return sectionDesigner.addSectionOutput(escalationTypeName, escalationTypeName, true);
                });

                // Obtain the escalation output
                SubSectionOutput escalationOutput = procedure.getSubSectionOutput(procedureEscalationType.getEscalationName());

                // Determine escalation by function handling first
                boolean isHandled = false;
                Map<String, String> escalations = functionConfig.getEscalations();
                if (escalations != null) {
                    String functionEscalationHandler = escalations.get(escalationTypeName);
                    if (functionEscalationHandler != null) {

                        // Obtain the handling escalation
                        SubSection handlingProcecure = procedures.get(functionEscalationHandler);

                        // TODO handle no procedure

                        // Handle escalation specifically
                        sectionDesigner.link(escalationOutput, handlingProcecure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
                        isHandled = true;
                    }
                }

                // Determine escalation handled generically by composition
                CompositionConfig composition = composeConfig.getComposition();
                if (composition != null) {
                    Map<String, String> compositionEscalations = composition.getEscalations();
                    if (compositionEscalations != null) {
                        String compositionEscalationHandler = compositionEscalations.get(escalationTypeName);
                        if (compositionEscalationHandler != null) {

                            // Obtain the handling escalation
                            SubSection handlingProcedure = procedures.get(compositionEscalationHandler);

                            // TODO handle no procedure

                            // Handle escalation by composition
                            sectionDesigner.link(escalationOutput, handlingProcedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
                            isHandled = true;
                        }
                    }
                }

                // Fall back to escalating outside composition
                if (!isHandled) {
                    sectionDesigner.link(escalationOutput, escalation);
                }
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
