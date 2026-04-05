package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.ComposeConfig;
import net.officefloor.activity.compose.section.ComposeSectionSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

public class ComposeEmployer {

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
                OfficeSection composition = architect.addOfficeSection(sectionName, ComposeSectionSource.class.getName(), resourceName);
                if (properties != null) {
                    properties.configureProperties(composition);
                }
                return composition;
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
            public <C extends ComposeConfig, T> T build(String sectionName, ComposeSource<T, C> source,
                                                        String resourceName, PropertyList properties,
                                                        Class<C> configuration) throws Exception {

                // Source the item
                T item = source.source(new ComposeContext<C>() {
                    @Override
                    public C getConfiguration() {
                        return null;
                    }

                    @Override
                    public OfficeArchitect getOfficeArchitect() {
                        return null;
                    }

                    @Override
                    public OfficeSourceContext getOfficeSourceContext() {
                        return null;
                    }

                    @Override
                    public OfficeSectionInput getStartFunction() {
                        return null;
                    }

                    @Override
                    public OfficeSectionInput getFunction(String functionName) {
                        return null;
                    }
                });

                // Return the item
                return item;
            }
        };
    }

}
