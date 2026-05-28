package net.officefloor.activity.compose.section;

import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.CompositionConfiguration;
import net.officefloor.activity.compose.FunctionConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
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
import net.officefloor.compile.section.SectionInputType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.section.SectionOutputType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionInput;
import net.officefloor.compile.spi.section.SectionObject;
import net.officefloor.compile.spi.section.SectionOutput;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.SubSectionInput;
import net.officefloor.compile.spi.section.SubSectionOutput;
import net.officefloor.compile.spi.section.source.SectionSource;
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

        // Load the composed functions
        Map<String, ComposedFunction> functions = new HashMap<>();
        Map<String, SectionObject> externalObjects = new HashMap<>();
        for (String functionName : this.composeConfiguration.getFunctions().keySet()) {
            FunctionConfiguration functionConfiguration = this.composeConfiguration.getFunctions().get(functionName);

            // Obtain details of function
            ComposedFunction composedFunction;
            String className = functionConfiguration.getClassName();
            String procedureSource = functionConfiguration.getProcedure();
            if (className != null) {

                // Capture the composed function
                composedFunction = ComposeSectionSource.loadProcedure(functionName, className,
                        functionConfiguration, sectionDesigner, sectionSourceContext,
                        procedureLoader, procedureArchitect, externalObjects);
                functions.put(functionName, composedFunction);

            } else if (procedureSource != null) {

                // Capture the composed function (custom procedure source)
                composedFunction = ComposeSectionSource.loadCustomProcedure(functionName, procedureSource,
                        functionConfiguration, sectionDesigner, sectionSourceContext,
                        procedureLoader, procedureArchitect, externalObjects);
                functions.put(functionName, composedFunction);

            } else {

                // Capture the composed function
                composedFunction = ComposeSectionSource.loadSectionSource(functionName, functionConfiguration,
                        sectionDesigner, sectionSourceContext, externalObjects);
                functions.put(functionName, composedFunction);
            }
        }

        // SectionOutputs created for #-prefixed external input references (deduplicated by name)
        Map<String, SectionOutput> externalSectionOutputs = new HashMap<>();

        // Map composition (following deterministic order)
        for (String composedFunctionName : new ArrayList<>(functions.keySet()).stream().sorted().toList()) {
            ComposedFunction composedFunction = functions.get(composedFunctionName);

            // Determine if next
            String next = composedFunction.getConfiguration().getNext();
            if ((next != null) && (!next.isEmpty())) {

                if (next.startsWith(ComposeEmployer.ADDED_INPUT_PREFIX)) {
                    // External input reference — route via a SectionOutput
                    String outputName = next.substring(ComposeEmployer.ADDED_INPUT_PREFIX.length());
                    SectionOutput externalOutput = externalSectionOutputs.computeIfAbsent(outputName,
                            (name) -> sectionDesigner.addSectionOutput(name, null, false));
                    sectionDesigner.link(composedFunction.getNextOutput(), externalOutput);

                } else {
                    // Obtain the next procedure
                    ComposedFunction nextFunction = functions.get(next);
                    if (nextFunction == null) {
                        // Unknown next function
                        sectionDesigner.addIssue("Function " + composedFunctionName + " has next configured to unknown function " + next);

                    } else {
                        // Map to procedure
                        sectionDesigner.link(composedFunction.getNextOutput(), nextFunction.getInput());
                    }
                }
            }

            // Map flow outputs
            link(composedFunction.getConfiguration().getOutputs(), null,
                    composedFunction.getOutputs(), ComposedFunctionOutput::getOutputName,
                    (handlerName) -> {
                        ComposedFunction handlingFunction = functions.get(handlerName);
                        return (handlingFunction != null) ? handlingFunction.getInput() : null;
                    }, (functionOutput, input) -> {
                        sectionDesigner.link(composedFunction.getOutput(functionOutput.getOutputName()), input);
                    }, (flowName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " has output " + flowName + " but it is not configured");
                    }, (functionOutput, handlerName) -> {
                        if (handlerName.startsWith(ComposeEmployer.ADDED_INPUT_PREFIX)) {
                            // External input reference — route via a SectionOutput
                            String outputName = handlerName.substring(ComposeEmployer.ADDED_INPUT_PREFIX.length());
                            SectionOutput externalOutput = externalSectionOutputs.computeIfAbsent(outputName,
                                    (name) -> sectionDesigner.addSectionOutput(name, functionOutput.getArgumentType(), false));
                            sectionDesigner.link(composedFunction.getOutput(functionOutput.getOutputName()), externalOutput);
                        } else {
                            sectionDesigner.addIssue("Function " + composedFunctionName + " has output " + functionOutput.getOutputName() + " that is configured to unknown function " + handlerName);
                        }
                    }, (flowName, handlerName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " configures output " + flowName + " but it does not output the flow");
                    });

            // Map escalations
            Map<String, SectionOutput> sectionEscalations = new HashMap<>();
            link(composedFunction.getConfiguration().getEscalations(), composeConfiguration.getComposition(),
                    composedFunction.getEscalations(), ComposedFunctionOutput::getOutputName,
                    (handlerName) -> {
                        ComposedFunction handlingFunction = functions.get(handlerName);
                        return (handlingFunction != null) ? handlingFunction.getInput() : null;
                    }, (functionOutput, input) -> {
                        sectionDesigner.link(composedFunction.getOutput(functionOutput.getOutputName()), input);
                    }, (functionOutput) -> {
                        // Unhandled escalation to be escalated out of section
                        SectionOutput escalation = sectionEscalations.computeIfAbsent(functionOutput.getOutputName(), (key) -> {
                            return sectionDesigner.addSectionOutput(functionOutput.getOutputName(), functionOutput.getArgumentType(), true);
                        });
                        sectionDesigner.link(composedFunction.getOutput(functionOutput.getOutputName()), escalation);
                    }, (functionOutput, handlerName) -> {
                        sectionDesigner.addIssue("Function " + composedFunctionName + " has escalation " + functionOutput.getOutputName() + " that is configured to unknown function " + handlerName);
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
                SectionInput externalInput = sectionDesigner.addSectionInput(externalAccessFunctionName,
                        accessedFunction.getParameterType());
                sectionDesigner.link(externalInput, accessedFunction.getInput());
            }
        }
    }

    private static interface ComposedFunction {
        FunctionConfiguration getConfiguration();
        SubSectionInput getInput();
        String getParameterType();
        SubSectionOutput getNextOutput();
        ComposedFunctionOutput[] getOutputs();
        ComposedFunctionOutput[] getEscalations();
        SubSectionOutput getOutput(String outputName);
    }

    private static class ComposedFunctionOutput {

        private final String outputName;

        private final String argumentType;

        public ComposedFunctionOutput(String outputName, String argumentType) {
            this.outputName = outputName;
            this.argumentType = argumentType;
        }

        public String getOutputName() {
            return this.outputName;
        }

        public String getArgumentType() {
            return this.argumentType;
        }
    }

    private static ComposedFunction loadProcedure(String functionName, String className, FunctionConfiguration functionConfiguration,
                               SectionDesigner sectionDesigner, SectionSourceContext sectionSourceContext,
                               ProcedureLoader procedureLoader, ProcedureArchitect<SubSection> procedureArchitect,
                               Map<String, SectionObject> externalObjects) {

        // Ensure the class available
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
                throw sectionDesigner.addIssue("Require configuring method for " + functionName
                        + " (" + className + ") as it contains multiple public methods ("
                        + Arrays.stream(procedureOptions).map((procedure) -> procedure.getProcedureName()).collect(Collectors.joining(", ")) + ")");
            }
        }

        // Load the configuration
        PropertyList properties = sectionSourceContext.createPropertyList();

        // Load procedure properties from context (convention: "{functionName}.procedure.{propertyName}")
        String procedurePropertyPrefix = functionName + ".procedure.";
        for (String propName : sectionSourceContext.getPropertyNames()) {
            if (propName.startsWith(procedurePropertyPrefix)) {
                String propKey = propName.substring(procedurePropertyPrefix.length());
                properties.addProperty(propKey).setValue(sectionSourceContext.getProperty(propName));
            }
        }

        // Load inline properties from YAML (as fallback if not already set from context)
        Map<String, String> configProperties = functionConfiguration.getProperties();
        if (configProperties != null) {
            for (Map.Entry<String, String> entry : configProperties.entrySet()) {
                if (properties.getProperty(entry.getKey()) == null) {
                    properties.addProperty(entry.getKey()).setValue(entry.getValue());
                }
            }
        }

        // Determine if next
        String next = functionConfiguration.getNext();
        boolean isNext = ((next != null) && (!next.isEmpty()));

        // Load the procedure
        SubSection procedure = procedureArchitect.addProcedure(functionName, className, "Class", methodName, isNext, properties);

        // Load the procedure type
        ProcedureType procedureType = procedureLoader.loadProcedureType(className, "Class", methodName, properties);

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

        // Return the composed function
        return new ProcedureComposedFunction(functionConfiguration, procedureType, procedure);
    }

    /**
     * Composed function for a {@link Procedure}.
     */
    private static class ProcedureComposedFunction implements ComposedFunction {

        private final FunctionConfiguration functionConfiguration;

        private final ProcedureType procedureType;

        private final SubSection procedure;

        private final ComposedFunctionOutput[] outputs;

        private final ComposedFunctionOutput[] escalations;

        public ProcedureComposedFunction(FunctionConfiguration functionConfiguration, ProcedureType procedureType, SubSection procedure) {
            this.functionConfiguration = functionConfiguration;
            this.procedureType = procedureType;
            this.procedure = procedure;

            // Create the outputs and escalations
            this.outputs = Arrays.asList(procedureType.getFlowTypes())
                    .stream().map((flowType) -> {
                        Class<?> argumentType = flowType.getArgumentType();
                        return new ComposedFunctionOutput(flowType.getFlowName(), (argumentType != null) ? argumentType.getName() : null);
                    }).toArray(ComposedFunctionOutput[]::new);
            this.escalations = Arrays.asList(procedureType.getEscalationTypes())
                    .stream().map((escalationType) -> {
                        Class<?> type = escalationType.getEscalationType();
                        return new ComposedFunctionOutput(escalationType.getEscalationName(), (type != null) ? type.getName() : null);
                    }).toArray(ComposedFunctionOutput[]::new);
        }

        /*
         * ================ ComposeFunction ===================
         */

        @Override
        public FunctionConfiguration getConfiguration() {
            return this.functionConfiguration;
        }

        @Override
        public SubSectionInput getInput() {
            return this.procedure.getSubSectionInput(ProcedureArchitect.INPUT_NAME);
        }

        @Override
        public String getParameterType() {
            Class<?> parameterType = this.procedureType.getParameterType();
            return (parameterType != null) ? parameterType.getName() : null;
        }

        @Override
        public SubSectionOutput getNextOutput() {
            return this.procedure.getSubSectionOutput(ProcedureArchitect.NEXT_OUTPUT_NAME);
        }

        @Override
        public ComposedFunctionOutput[] getOutputs() {
            return this.outputs;
        }

        @Override
        public ComposedFunctionOutput[] getEscalations() {
            return this.escalations;
        }

        @Override
        public SubSectionOutput getOutput(String outputName) {
            return this.procedure.getSubSectionOutput(outputName);
        }
    }

    private static ComposedFunction loadCustomProcedure(String functionName, String procedureSourceName,
                               FunctionConfiguration functionConfiguration,
                               SectionDesigner sectionDesigner, SectionSourceContext sectionSourceContext,
                               ProcedureLoader procedureLoader, ProcedureArchitect<SubSection> procedureArchitect,
                               Map<String, SectionObject> externalObjects) {

        // Resource is optional for custom procedure sources
        String resource = functionConfiguration.getResource();

        // Determine the method name
        String methodName = functionConfiguration.getMethod();
        if (methodName == null) {
            Procedure[] allProcedures = procedureLoader.listProcedures(resource);
            Procedure[] sourceProcedures = Arrays.stream(allProcedures)
                    .filter(p -> procedureSourceName.equals(p.getServiceName()))
                    .toArray(Procedure[]::new);
            if (sourceProcedures.length == 1) {
                methodName = sourceProcedures[0].getProcedureName();
            } else {
                throw sectionDesigner.addIssue("Require configuring method for " + functionName
                        + " (procedure source: " + procedureSourceName + ") as it contains multiple procedures ("
                        + Arrays.stream(sourceProcedures).map(Procedure::getProcedureName).collect(Collectors.joining(", ")) + ")");
            }
        }

        // Determine if next
        String next = functionConfiguration.getNext();
        boolean isNext = ((next != null) && (!next.isEmpty()));

        // Load the procedure
        PropertyList properties = new PropertyListImpl();
        SubSection procedure = procedureArchitect.addProcedure(functionName, resource, procedureSourceName, methodName, isNext, properties);

        // Load the procedure type
        ProcedureType procedureType = procedureLoader.loadProcedureType(resource, procedureSourceName, methodName, properties);

        // Load the object dependencies
        for (ProcedureObjectType procedureObjectType : procedureType.getObjectTypes()) {
            String objectType = procedureObjectType.getObjectType().getName();
            String objectTypeQualifier = procedureObjectType.getTypeQualifier();
            String objectName = ((objectTypeQualifier != null) ? objectTypeQualifier + "_" : "") + objectType;
            SectionObject externalObject = externalObjects.computeIfAbsent(objectName, (key) -> {
                SectionObject object = sectionDesigner.addSectionObject(objectName, objectType);
                if (objectTypeQualifier != null) {
                    object.setTypeQualifier(objectTypeQualifier);
                }
                return object;
            });
            sectionDesigner.link(procedure.getSubSectionObject(objectName), externalObject);
        }

        return new ProcedureComposedFunction(functionConfiguration, procedureType, procedure);
    }

    private static ComposedFunction loadSectionSource(String functionName, FunctionConfiguration functionConfiguration,
                               SectionDesigner sectionDesigner, SectionSourceContext sectionSourceContext,
                               Map<String, SectionObject> externalObjects) {

        String sectionSourceClassName = functionConfiguration.getSource();
        String location = functionConfiguration.getLocation();

        // Build the properties
        PropertyList properties = new PropertyListImpl();
        Map<String, String> configProperties = functionConfiguration.getProperties();
        if (configProperties != null) {
            for (Map.Entry<String, String> entry : configProperties.entrySet()) {
                properties.addProperty(entry.getKey()).setValue(entry.getValue());
            }
        }

        // Load the section type to determine inputs
        SectionType sectionType = sectionSourceContext.loadSectionType(functionName, sectionSourceClassName, location, properties);

        // Determine the input to use
        SectionInputType[] inputTypes = sectionType.getSectionInputTypes();
        SectionInputType inputType;
        String inputName;
        if (inputTypes.length == 1) {
            inputType = inputTypes[0];
            inputName = inputType.getSectionInputName();
        } else {
            inputName = functionConfiguration.getInput();
            if (inputName == null) {
                throw sectionDesigner.addIssue("Require configuring input for " + functionName
                        + " (" + sectionSourceClassName + ") as it contains multiple inputs ("
                        + Arrays.stream(inputTypes).map(SectionInputType::getSectionInputName).collect(Collectors.joining(", ")) + ")");
            }
            final String finalInputName = inputName;
            inputType = Arrays.stream(inputTypes)
                    .filter(it -> finalInputName.equals(it.getSectionInputName()))
                    .findFirst().orElse(null);
            if (inputType == null) {
                throw sectionDesigner.addIssue("Configured input '" + inputName + "' not found in " + functionName
                        + " (" + sectionSourceClassName + ")");
            }
        }

        // Create the subsection
        SubSection subSection = sectionDesigner.addSubSection(functionName, sectionSourceClassName, location);
        properties.configureProperties(subSection);

        // Load the object dependencies
        for (SectionObjectType objectType : sectionType.getSectionObjectTypes()) {
            String objectTypeName = objectType.getObjectType();
            String typeQualifier = objectType.getTypeQualifier();
            String objectName = ((typeQualifier != null) ? typeQualifier + "_" : "") + objectTypeName;
            SectionObject externalObject = externalObjects.computeIfAbsent(objectName, (key) -> {
                SectionObject object = sectionDesigner.addSectionObject(objectName, objectTypeName);
                if (typeQualifier != null) {
                    object.setTypeQualifier(typeQualifier);
                }
                return object;
            });
            sectionDesigner.link(subSection.getSubSectionObject(objectType.getSectionObjectName()), externalObject);
        }

        return new SectionSourceComposedFunction(functionConfiguration, sectionType, inputName, inputType, subSection, sectionDesigner);
    }

    /**
     * Composed function for the {@link SectionSource}.
     */
    private static class SectionSourceComposedFunction implements ComposedFunction {

        private final FunctionConfiguration functionConfiguration;

        private final String inputName;

        private final SectionInputType inputType;

        private final SubSection subSection;

        private final ComposedFunctionOutput[] outputs;

        private final ComposedFunctionOutput[] escalations;

        private final SectionDesigner sectionDesigner;

        public SectionSourceComposedFunction(FunctionConfiguration functionConfiguration, SectionType sectionType,
                                             String inputName, SectionInputType inputType, SubSection subSection,
                                             SectionDesigner sectionDesigner) {
            this.functionConfiguration = functionConfiguration;
            this.inputName = inputName;
            this.inputType = inputType;
            this.subSection = subSection;
            this.sectionDesigner = sectionDesigner;
            this.outputs = Arrays.stream(sectionType.getSectionOutputTypes())
                    .filter(outputType -> !outputType.isEscalationOnly())
                    .map(outputType -> new ComposedFunctionOutput(outputType.getSectionOutputName(), outputType.getArgumentType()))
                    .toArray(ComposedFunctionOutput[]::new);
            this.escalations = Arrays.stream(sectionType.getSectionOutputTypes())
                    .filter(SectionOutputType::isEscalationOnly)
                    .map(outputType -> new ComposedFunctionOutput(outputType.getSectionOutputName(), outputType.getArgumentType()))
                    .toArray(ComposedFunctionOutput[]::new);
        }

        /*
         * ================ ComposeFunction ===================
         */

        @Override
        public FunctionConfiguration getConfiguration() {
            return this.functionConfiguration;
        }

        @Override
        public SubSectionInput getInput() {
            return this.subSection.getSubSectionInput(this.inputName);
        }

        @Override
        public String getParameterType() {
            return this.inputType.getParameterType();
        }

        @Override
        public SubSectionOutput getNextOutput() {
            throw this.sectionDesigner.addIssue("Can not configure next for " + SectionSource.class.getSimpleName());
        }

        @Override
        public ComposedFunctionOutput[] getOutputs() {
            return this.outputs;
        }

        @Override
        public ComposedFunctionOutput[] getEscalations() {
            return this.escalations;
        }

        @Override
        public SubSectionOutput getOutput(String outputName) {
            return this.subSection.getSubSectionOutput(outputName);
        }
    }


}
