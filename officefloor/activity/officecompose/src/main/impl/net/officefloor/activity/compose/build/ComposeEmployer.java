package net.officefloor.activity.compose.build;

import net.officefloor.activity.compose.section.ComposeSectionSource;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;

public class ComposeEmployer {

    /**
     * Employs the {@link ComposeArchitect}.
     *
     * @param architect {@link OfficeArchitect}.
     * @param context {@link OfficeSourceContext}.
     * @return {@link ComposeArchitect}.
     */
    public static ComposeArchitect<OfficeSection> employComposeArchitect(OfficeArchitect architect, OfficeSourceContext context) {
        return new ComposeArchitect<OfficeSection>() {

            @Override
            public OfficeSection addComposition(String sectionName, String resourceName, PropertyList properties) {
                OfficeSection composition = architect.addOfficeSection(sectionName, ComposeSectionSource.class.getName(), resourceName);
                return composition;
            }
        };
    }

}
