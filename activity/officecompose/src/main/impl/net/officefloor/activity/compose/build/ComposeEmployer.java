package net.officefloor.activity.compose.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.section.ComposeSectionSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

import java.io.IOException;
import java.io.Reader;

public class ComposeEmployer {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Employs the {@link ComposeArchitect}.
     *
     * @param architect {@link OfficeArchitect}.
     * @param context   {@link OfficeSourceContext}.
     * @return {@link ComposeArchitect}.
     */
    public static ComposeArchitect<OfficeSection> employComposeArchitect(OfficeArchitect architect, OfficeSourceContext context) {
        return new ComposeArchitect<OfficeSection>() {

            @Override
            public OfficeSection addComposition(String sectionName, String resourceName, PropertyList properties) {
                return buildItem(architect, context, sectionName, ComposeContext::getCompositionSection, resourceName, properties, ComposeConfiguration.class);
            }
        };
    }

    /**
     * Employs the {@link ComposeBuilder}.
     *
     * @param architect {@link OfficeArchitect}.
     * @param context   {@link OfficeSourceContext}.
     * @return {@link ComposeBuilder}.
     */
    public static ComposeBuilder employComposeBuilder(OfficeArchitect architect, OfficeSourceContext context) {
        return new ComposeBuilder() {

            @Override
            public <C extends ComposeConfiguration, T> T build(String sectionName, ComposeSource<T, C> source,
                                                               String resourceName, PropertyList properties,
                                                               Class<C> configurationClass) throws Exception {
                return buildItem(architect, context, sectionName, source, resourceName, properties, configurationClass);
            }
        };
    }

    private static <T, C extends ComposeConfiguration> T buildItem(OfficeArchitect architect, OfficeSourceContext sourceContext,
                                                                  String sectionName, ComposeSource<T, C> source,
                                                                  String resourceName, PropertyList properties, Class<C> configurationClass) {

        // Load the YAML composition
        Reader compositionConfiguration = sourceContext
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
        try {
            return source.source(new ComposeContext<C>() {

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
                    return sourceContext;
                }

                @Override
                public OfficeSectionInput getStartFunction() {
                    return composition.getOfficeSectionInput(ComposeArchitect.INPUT_NAME);
                }

                @Override
                public OfficeSectionInput getFunction(String functionName) {
                    composeSectionSource.addExternalAccessFunction(functionName);
                    return composition.getOfficeSectionInput(functionName);
                }

                @Override
                public OfficeSection getCompositionSection() {
                    return composition;
                }
            });
        } catch (Exception ex) {
            throw architect.addIssue("Failed to source item from " + resourceName, ex);
        }
    }

}
