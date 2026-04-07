package net.officefloor.activity.compose.section;

import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.CompositionConfiguration;
import net.officefloor.activity.compose.FunctionConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.procedure.Procedure;
import net.officefloor.activity.procedure.ProcedureEscalationType;
import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.activity.procedure.ProcedureLoader;
import net.officefloor.activity.procedure.ProcedureObjectType;
import net.officefloor.activity.procedure.ProcedureType;
import net.officefloor.activity.procedure.build.ProcedureArchitect;
import net.officefloor.activity.procedure.build.ProcedureEmployer;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSectionInput;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * {@link net.officefloor.compile.spi.section.source.SectionSource} for
 */
public class ComposeSectionSource extends AbstractSectionSource {

    /**
     * Links flow to handler based on configuration.
     *
     * @param configuration              Configuration of flow to handler function.
     * @param compositionConfiguration   {@link CompositionConfiguration} for composition escalation handling. Should be <code>null</code> for flow handling.
     * @param flowTypes                  Flow types.
     * @param getFlowName                Means to obtain flow name from flow type.
     * @param getHandler                 Obtains the handler for the configured handler name.
     * @param link                       Links the flow to the handler.
     * @param nonConfiguredFlowHandler   Invoked for flows not configured.
     * @param noHandlerAvailable         Invoked if <code>null</code> handler provided for flow.
     * @param extraConfiguredFlowHandler Invoked for configuration of a flow that does not exist in the flow types.
     * @param <F>                        Flow type.
     * @param <H>                        Handler type.
     */
    public static <F, H> void link(Map<String, String> configuration, CompositionConfiguration compositionConfiguration,
                                   F[] flowTypes, Function<F, String> getFlowName, Function<String, H> getHandler,
                                   BiConsumer<F, H> link,
                                   Consumer<F> nonConfiguredFlowHandler, BiConsumer<F, String> noHandlerAvailable,
                                   BiConsumer<String, String> extraConfiguredFlowHandler) {

        // Create the mapping configuration
        Map<String, String> mapping = new HashMap<>();

        // Load compose escalation configuration (if available)
        if (compositionConfiguration != null) {
            Map<String, String> compositionEscalations = compositionConfiguration.getEscalations();
            if (compositionEscalations != null) {
                mapping.putAll(compositionEscalations);
            }
        }

        // Overwrite with specific configuration
        if (configuration != null) {
            mapping.putAll(configuration);
        }

        // Capture all configured flows to determine extra configured
        Set<String> trackConfiguration = new HashSet<>(mapping.keySet());

        // Configure the links
        for (F flowType : flowTypes) {
            String flowName = getFlowName.apply(flowType);

            // Determine handler
            String handlerName = mapping.get(flowName);
            if (handlerName == null) {
                // Handler for flow not configured
                nonConfiguredFlowHandler.accept(flowType);

            } else {
                // Link flow to handler (if handler available)
                H handler = getHandler.apply(handlerName);
                if (handler == null) {
                    // No handler available for the flow
                    noHandlerAvailable.accept(flowType, handlerName);

                } else {
                    // Link flow to handler
                    link.accept(flowType, handler);
                }

                // Remove as configuration used
                trackConfiguration.remove(flowName);
            }
        }

        // Flag any extra configurations (in deterministic order)
        for (String flowName : new ArrayList<>(trackConfiguration).stream().sorted().toList()) {
            if ((configuration != null) && (configuration.containsKey(flowName))) {

                // Specific configuration not mapped
                String handlerName = mapping.get(flowName);
                extraConfiguredFlowHandler.accept(flowName, handlerName);
            }
        }
    }

    /**
     * {@link ComposeConfiguration}.
     */
    private final ComposeConfiguration composeConfiguration;

    /**
     * Names of the functions to be accessible externally with handler should function not be configured.
     */
    private final Map<String, Consumer<String>> externalAccessedFunctions = new HashMap<>();

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
     *
     * @param functionName        Name of function to externally expose.
     * @param handleNotConfigured {@link Consumer} to handle the function not being configured.
     */
    public void addExternalAccessFunction(String functionName, Consumer<String> handleNotConfigured) {
        this.externalAccessedFunctions.put(functionName, handleNotConfigured);
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
                if (nextFunction == null) {
                    // Unknown next function
                    sectionDesigner.addIssue("Function " + composedFunctionName + " has next configured to unknown function " + next);

                } else {
                    // Map to procedure
                    sectionDesigner.link(composedFunction.procedure.getSubSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME),
                            nextFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
                }
            }

            // Map flow outputs
            link(composedFunction.functionConfiguration.getOutputs(), null,
                    composedFunction.procedureType.getFlowTypes(), ProcedureFlowType::getFlowName,
                    (handlerName) -> {
                        ComposedFunction handlingFunction = functions.get(handlerName);
                        return (handlingFunction != null) ? handlingFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME) : null;
                    }, (flowType, input) -> {
                        sectionDesigner.link(composedFunction.procedure.getSubSectionOutput(flowType.getFlowName()), input);
                    }, (flowType) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " has output " + flowType.getFlowName() + " but it is not configured");
                    }, (flowType, handlerName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " has output " + flowType.getFlowName() + " that is configured to unknown function " + handlerName);
                    }, (flowName, handlerName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " configures output " + flowName + " but it does not output the flow");
                    });

            // Map escalations
            Map<Class<?>, SectionOutput> sectionEscalations = new HashMap<>();
            link(composedFunction.functionConfiguration.getEscalations(), composeConfiguration.getComposition(),
                    composedFunction.procedureType.getEscalationTypes(), (escalationType) -> escalationType.getEscalationType().getName(),
                    (handlerName) -> {
                        ComposedFunction handlingFunction = functions.get(handlerName);
                        return (handlingFunction != null) ? handlingFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME) : null;
                    }, (flowType, input) -> {
                        sectionDesigner.link(composedFunction.procedure.getSubSectionOutput(flowType.getEscalationName()), input);
                    }, (flowType) -> {
                        // Unhandled escalation to be escalated out of section
                        SectionOutput escalation = sectionEscalations.computeIfAbsent(flowType.getEscalationType(), (key) -> {
                            return sectionDesigner.addSectionOutput(flowType.getEscalationType().getName(), flowType.getEscalationType().getName(), true);
                        });
                        sectionDesigner.link(composedFunction.procedure.getSubSectionOutput(flowType.getEscalationName()), escalation);
                    }, (flowType, handlerName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " has escalation " + flowType.getEscalationType().getName() + " that is configured to unknown function " + handlerName);
                    }, (flowName, handlerName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " configures escalation " + flowName + " but it does not escalate " + flowName);
                    });
        }

        // Include the start function for external access
        String startFunctionName = composeConfiguration.getStart();
        if (startFunctionName != null) {
            this.externalAccessedFunctions.put(composeConfiguration.getStart(), (start) -> {
                sectionDesigner.addIssue("Invalid configuration as start function " + start + " is not configured");
            });
        }

        // Make necessary functions accessible externally (in deterministic order)
        for (String externalAccessFunctionName : new ArrayList<>(this.externalAccessedFunctions.keySet()).stream().sorted().toList()) {

            // Obtain the function
            ComposedFunction accessedFunction = functions.get(externalAccessFunctionName);
            if (accessedFunction == null) {
                // Flag handler function does not exist
                Consumer<String> handleNotConfigured = this.externalAccessedFunctions.get(externalAccessFunctionName);
                if (handleNotConfigured != null) {
                    handleNotConfigured.accept(externalAccessFunctionName);
                }

            } else {
                // Make available externally
                Class<?> parameterType = accessedFunction.procedureType.getParameterType();
                SectionInput externalInput = sectionDesigner.addSectionInput(externalAccessFunctionName,
                        (parameterType != null) ? parameterType.getName() : null);
                sectionDesigner.link(externalInput, accessedFunction.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME));
            }
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
