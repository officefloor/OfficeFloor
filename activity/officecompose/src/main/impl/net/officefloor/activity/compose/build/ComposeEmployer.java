package net.officefloor.activity.compose.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.FunctionConfiguration;
import net.officefloor.activity.compose.section.ComposeSectionSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionFunction;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSubSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ComposeEmployer {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Employs the {@link ComposeArchitect}.
     *
     * @param architect {@link OfficeArchitect}.
     * @param context   {@link OfficeSourceContext}.
     * @return {@link ComposeArchitect}.
     */
    public static ComposeArchitect employComposeArchitect(OfficeArchitect architect, OfficeSourceContext context) {
        return new ComposeArchitect() {

            private final Map<String, OfficeGovernance> governances = new HashMap<>();

            @Override
            public void addGovernance(String governanceName, OfficeGovernance goverance) {
                this.governances.put(governanceName, goverance);
            }

            @Override
            public <C extends ComposeConfiguration, T> T addComposition(String sectionName, ComposeSource<T, C> source,
                                                               String resourceName, PropertyList properties,
                                                               Class<C> configurationClass) throws Exception {

                // Load the YAML composition
                Reader compositionConfiguration = context
                        .getConfigurationItem(resourceName, properties)
                        .getReader();
                C composeConfiguration;
                try {
                    composeConfiguration = MAPPER.readValue(compositionConfiguration, configurationClass);
                } catch (IOException ex) {
                    throw architect.addIssue("Failed to read configuration " + resourceName + " for " + configurationClass.getName(), ex);
                }

                // Add the composition
                ComposeSectionSource composeSectionSource = new ComposeSectionSource(composeConfiguration);
                OfficeSection composition = architect.addOfficeSection(sectionName, composeSectionSource, resourceName);

                // Build the item
                T item;
                try {
                    item = source.source(new ComposeContext<C>() {

                        @Override
                        public String getItemName() {
                            return sectionName;
                        }

                        @Override
                        public C getConfiguration() {
                            return composeConfiguration;
                        }

                        @Override
                        public OfficeArchitect getOfficeArchitect() {
                            return architect;
                        }

                        @Override
                        public OfficeSourceContext getOfficeSourceContext() {
                            return context;
                        }

                        @Override
                        public OfficeSectionInput getStartFunction() {
                            return composition.getOfficeSectionInput(this.getConfiguration().getStart());
                        }

                        @Override
                        public OfficeSectionInput getFunction(String functionName, Consumer<String> handleNotConfigured) {
                            composeSectionSource.addExternalAccessFunction(functionName, handleNotConfigured);
                            return composition.getOfficeSectionInput(functionName);
                        }

                        @Override
                        public <F> void linkFlows(Map<String, String> configuration, F[] flowTypes, ComposeLinkHandler<F> linkHandler) {
                            ComposeSectionSource.link(configuration, null, flowTypes, linkHandler::getFlowName, (handlerName) -> {
                                return this.getFunction(handlerName, null);
                            }, linkHandler::link, linkHandler::handleNonConfiguredFlow, linkHandler::handleNoHandlingFunction, linkHandler::handleExtraConfiguredFlow);
                        }

                        @Override
                        public <E> void linkEscalations(Map<String, String> configuration, E[] escalationTypes, ComposeLinkHandler<E> linkHandler) {
                            ComposeSectionSource.link(configuration, composeConfiguration.getComposition(), escalationTypes, linkHandler::getFlowName, (handlerName) -> {
                                return this.getFunction(handlerName, null);
                            }, linkHandler::link, linkHandler::handleNonConfiguredFlow, linkHandler::handleNoHandlingFunction, linkHandler::handleExtraConfiguredFlow);
                        }

                        @Override
                        public OfficeSection getCompositionSection() {
                            return composition;
                        }
                    });

                } catch (Exception ex) {
                    throw architect.addIssue("Failed to source item from " + resourceName, ex);
                }

                // Add the governance
                composeConfiguration.getFunctions().forEach((functionName, functionConfiguration) -> {

                    // Obtain the governance
                    List<String> govern = functionConfiguration.getGovern();
                    if (govern != null) {

                        // Obtain the sub section for the function
                        OfficeSubSection functionSection = composition.getOfficeSubSection(functionName);

                        // Link the governance
                        for (String requiredGovernance : govern) {

                            // Obtain the governance
                            OfficeGovernance governance = this.governances.get(requiredGovernance);
                            if (governance == null) {
                                // Unknown governance required
                                architect.addIssue("Function " + functionName + " requires governance " + requiredGovernance + " but this governance is not configured");

                            } else {
                                // Govern the function
                                functionSection.addGovernance(governance);
                            }
                        }
                    }
                });

                // Return the item
                return item;
            }

            @Override
            public <C extends ComposeConfiguration, T> void addCompositions(ComposeSource<T, C> source, String resourceDirectory,
                                                                  PropertyList properties, Class<C> configurationClass,
                                                                  ComposeListener<T> listener) throws Exception {

                // Determine the resource prefix
                while (resourceDirectory.endsWith("/")) {
                    resourceDirectory = resourceDirectory.substring(0, resourceDirectory.length() - 1);
                }
                resourceDirectory = resourceDirectory + "/";

                // Load the resources
                try (ScanResult result = new ClassGraph().acceptPaths(resourceDirectory).scan()) {
                    for (String yamlExtension : new String[] { "yml", "yaml"}) {
                        for (Resource resource : result.getResourcesWithExtension(yamlExtension)) {

                            // Obtain the path
                            String classpathResourcePath = resource.getPath();
                            String resourcePath = classpathResourcePath.substring(resourceDirectory.length());

                            // Obtain the section name (full file name minus extension)
                            String sectionName = resourcePath.substring(0, resourcePath.length() - (".".length() + yamlExtension.length()));

                            // Build the item
                            T item = this.addComposition(sectionName, source, classpathResourcePath, properties, configurationClass);

                            // Notify of the item
                            listener.composition(sectionName, item);
                        }
                    }
                }
            }
        };
    }

}
