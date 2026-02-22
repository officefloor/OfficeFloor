package net.officefloor.web.rest.build;

import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.HttpInput;
import net.officefloor.web.build.WebArchitect;

public class RestEmployer {

    /**
     * Employs the {@link RestArchitect}.
     *
     * @param officeArchitect  {@link OfficeArchitect}.
     * @param webArchitect     {@link WebArchitect}.
     * @param composeArchitect {@link ComposeArchitect}.
     * @param context          {@link OfficeSourceContext}.
     * @return {@link RestArchitect}.
     */
    public static RestArchitect employRestArchitect(OfficeArchitect officeArchitect, WebArchitect webArchitect, ComposeArchitect<OfficeSection> composeArchitect, OfficeSourceContext context) {
        return new RestArchitect() {

            @Override
            public void addRestService(boolean isSecure, String restPath, HttpMethod method, String compositionLocation, PropertyList properties) {

                // Obtain the REST input
                HttpInput input = webArchitect.getHttpInput(isSecure, method.getName(), restPath);

                // Compose servicing
                OfficeSection servicing = composeArchitect.addComposition("REST_" + method.getName() + "_" + restPath, compositionLocation, properties);

                // Handle REST request
                officeArchitect.link(input.getInput(), servicing.getOfficeSectionInput(ComposeArchitect.INPUT_NAME));
            }
        };
    }

}
