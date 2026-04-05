package net.officefloor.activity.compose.section;

import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.CompositionConfiguration;
import net.officefloor.activity.compose.FunctionConfiguration;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link net.officefloor.compile.spi.section.source.SectionSource} for
 */
public class ComposeSectionSource extends AbstractSectionSource {

    /**
     * {@link ComposeConfiguration}.
     */
    private final ComposeConfiguration composeConfiguration;

    /**
     * Names of the functions to be accessible externally.
     */
    private final Set<String> externalAccessedFunctions = new HashSet<>();

    /**
     * Instantiate.
     *
     * @param composeConfig {@link ComposeConfiguration}.
     */
    public ComposeSectionSource(ComposeConfiguration composeConfig) {
        this.composeConfiguration = composeConfig;
    }

    /**
     * Flags a composed function is to be externally accessed.
     */
    public void addExternalAccessFunction(String functionName) {
        this.externalAccessedFunctions.add(functionName);
    }

    /*
     * ==================== SectionSource ===========================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {
        // No specification
    }

    @Override
    public void sourceSection(SectionDesigner sectionDesigner, SectionSourceContext sectionSourceContext) throws Exception {

        // Create the procedure architect
        ProcedureLoader procedureLoader = ProcedureEmployer.employProcedureLoader(sectionDesigner, sectionSourceContext);
        ProcedureArchitect<SubSection> procedureArchitect = ProcedureEmployer.employProcedureDesigner(sectionDesigner, sectionSourceContext);

        // Capture the initial procedure
        String serviceName = this.composeConfiguration.getStart();
        ProcedureType serviceProcecureType = null;
        SubSection serviceProcedure = null;


        // Load the procedures
        Map<String, ComposedFunction> functions = new HashMap<>();
        Map<String, SectionObject> externalObjects = new HashMap<>();
        for (String procedureName : this.composeConfiguration.getFunctions().keySet()) {
            FunctionConfiguration functionConfiguration = this.composeConfiguration.getFunctions().get(procedureName);

            // Obtain details of function (ensuring class available)
            String className = functionConfiguration.getClassName();
            sectionSourceContext.loadClass(className);

            // Determine the method name
            String methodName;
            Procedure[] procedureOptions = procedureLoader.listProcedures(className);
            if (procedureOptions != null && procedureOptions.length == 1) {
                // Use the only method configured
                methodName = procedureOptions[0].getProcedureName();
            } else {
                // Rely on configuration
                methodName = functionConfiguration.getMethod();

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
            String next = functionConfiguration.getNext();
            boolean isNext = ((next != null) && (!next.isEmpty()));

            // Load the procedure
            SubSection procedure = procedureArchitect.addProcedure(procedureName, className, "Class", methodName, isNext, properties);

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

            // Capture the composed function
            functions.put(procedureName, new ComposedFunction(functionConfiguration, procedureType, procedure));
        }

        // Map composition (following deterministic order)
        for (String composedFunctionName : new ArrayList<>(functions.keySet()).stream().sorted().toList()) {
            ComposedFunction composedFunction = functions.get(composedFunctionName);

            // Determine if next
            String next = composedFunction.functionConfiguration.getNext();
            if ((next != null) && (!next.isEmpty())) {

                // Obtain the next procedure
                ComposedFunction nextFunction = functions.get(next);

                // TODO handle no procedure

                // Map to procedure
                sectionDesigner.link(composedFunction.procedure.getSubSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
                        nextFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
            }

            // Map flow outputs
            Map<String, String> outputs = composedFunction.functionConfiguration.getOutputs();
            if (outputs != null) {
                for (String outputName : outputs.keySet()) {

                    // Obtain the handling procedure
                    String handlingProcedureName = outputs.get(outputName);
                    ComposedFunction handlingFunction = functions.get(handlingProcedureName);

                    // TODO handle no procedure

                    // Map to procedure
                    sectionDesigner.link(composedFunction.procedure.getSubSectionOutput(outputName),
                            handlingFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
                }
            }

            // Map escalations
            Map<Class<?>, SectionOutput> sectionEscalations = new HashMap<>();
            for (ProcedureEscalationType procedureEscalationType : composedFunction.procedureType.getEscalationTypes()) {

                // Obtain the escalation type
                Class<?> escalationType = procedureEscalationType.getEscalationType();
                String escalationTypeName = escalationType.getName();

                // Obtain the external escalation
                SectionOutput escalation = sectionEscalations.computeIfAbsent(escalationType, (key) -> {
                    return sectionDesigner.addSectionOutput(escalationTypeName, escalationTypeName, true);
                });

                // Obtain the escalation output
                SubSectionOutput escalationOutput = composedFunction.procedure.getSubSectionOutput(procedureEscalationType.getEscalationName());

                // Determine escalation by function handling first
                boolean isHandled = false;
                Map<String, String> escalations = composedFunction.functionConfiguration.getEscalations();
                if (escalations != null) {
                    String functionEscalationHandler = escalations.get(escalationTypeName);
                    if (functionEscalationHandler != null) {

                        // Obtain the handling escalation
                        ComposedFunction handlingFunction = functions.get(functionEscalationHandler);

                        // TODO handle no procedure

                        // Handle escalation specifically
                        sectionDesigner.link(escalationOutput, handlingFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
                        isHandled = true;
                    }
                }

                // Determine escalation handled generically by composition
                CompositionConfiguration composition = composeConfiguration.getComposition();
                if (composition != null) {
                    Map<String, String> compositionEscalations = composition.getEscalations();
                    if (compositionEscalations != null) {
                        String compositionEscalationHandler = compositionEscalations.get(escalationTypeName);
                        if (compositionEscalationHandler != null) {

                            // Obtain the handling escalation
                            ComposedFunction handlingFunction = functions.get(compositionEscalationHandler);

                            // TODO handle no procedure

                            // Handle escalation by composition
                            sectionDesigner.link(escalationOutput, handlingFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
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

        // Include the start function for external access
        this.externalAccessedFunctions.add(composeConfiguration.getStart());

        // Make necessary functions accessible externally
        for (String externalAccessFunctionName : this.externalAccessedFunctions) {

            // Obtain the function
            ComposedFunction accessedFunction = functions.get(externalAccessFunctionName);

            // TODO handle no procedure

            // Make available externally
            Class<?> parameterType = accessedFunction.procedureType.getParameterType();
            SectionInput externalInput = sectionDesigner.addSectionInput(externalAccessFunctionName,
                    (parameterType != null) ? parameterType.getName() : null);
            sectionDesigner.link(externalInput, accessedFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
        }
    }

    /**
     * Composed function.
     */
    private static class ComposedFunction {

        private final FunctionConfiguration functionConfiguration;

        private final ProcedureType procedureType;

        private final SubSection procedure;

        public ComposedFunction(FunctionConfiguration functionConfiguration, ProcedureType procedureType, SubSection procedure) {
            this.functionConfiguration = functionConfiguration;
            this.procedureType = procedureType;
            this.procedure = procedure;
        }
    }
}
