package net.officefloor.web.rest.build;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
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
    public static RestArchitect employRestArchitect(OfficeArchitect officeArchitect, WebArchitect webArchitect, ComposeArchitect composeArchitect, OfficeSourceContext context) {
        return new RestArchitect() {

            @Override
            public RestEndpoint addRestService(boolean isSecure, HttpMethod method, String restPath,
                                               String compositionLocation, PropertyList properties) throws Exception {

                // Compose servicing
                OfficeSectionInput serviceInput = composeArchitect.addComposition("REST_" + method.getName() + "_" + restPath,
                        ComposeContext::getStartFunction, compositionLocation, properties, ComposeConfiguration.class);

                // Return the Rest Endpoint
                return createRestEndpoint(isSecure, method, restPath, serviceInput, webArchitect, officeArchitect);
            }

            @Override
            public void addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties,
                                        RestEndpointListener listener) throws Exception {
                composeArchitect.addCompositions(ComposeContext::getStartFunction,
                        resourceDirectory, properties, ComposeConfiguration.class,
                        (composePath, serviceInput) -> {

                            // Split to method and path
                            int index = composePath.lastIndexOf('.');
                            if (index > 0) {

                                // Obtain the method and path
                                String method = composePath.substring(index + ".".length());
                                String path = composePath.substring(0, index);

                                // Handle root
                                if ("index".equalsIgnoreCase(path)) {
                                    path = "/";
                                }

                                // Inform configuring end point
                                RestEndpointContextImpl endpointContext = new RestEndpointContextImpl(isSecure, HttpMethod.getHttpMethod(method.toUpperCase()), path);
                                if (listener != null) {
                                    listener.initialise(endpointContext);
                                }

                                // Add the REST path
                                RestEndpoint endpoint = createRestEndpoint(
                                        endpointContext.isSecure(), endpointContext.getHttpMethod(), endpointContext.getPath(),
                                        serviceInput, webArchitect, officeArchitect);

                                // Inform listener
                                if (listener != null) {
                                    listener.endpoint(endpoint);
                                }
                            }
                        });
            }
        };
    }

    protected static RestEndpoint createRestEndpoint(boolean isSecure, HttpMethod method, String restPath,
                                                     OfficeSectionInput serviceInput,
                                                     WebArchitect webArchitect, OfficeArchitect officeArchitect) {

        // Obtain the REST input
        HttpInput httpInput = webArchitect.getHttpInput(isSecure, method.getName(), restPath);

        // Handle REST request
        officeArchitect.link(httpInput.getInput(), serviceInput);

        // Return the rest end point
        return new RestEndpointImpl(isSecure, method, restPath, httpInput, serviceInput);
    }

}
