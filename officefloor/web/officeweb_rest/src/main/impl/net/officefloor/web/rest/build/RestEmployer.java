package net.officefloor.web.rest.build;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
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
            public RestEndpoint addRestService(boolean isSecure, HttpMethod method, String restPath, String compositionLocation, PropertyList properties) {

                // Obtain the REST input
                HttpInput input = webArchitect.getHttpInput(isSecure, method.getName(), restPath);

                // Compose servicing
                OfficeSection servicing = composeArchitect.addComposition("REST_" + method.getName() + "_" + restPath, compositionLocation, properties);

                // Handle REST request
                officeArchitect.link(input.getInput(), servicing.getOfficeSectionInput(ComposeArchitect.INPUT_NAME));

                // Return the rest end point
                return new RestEndpointImpl(isSecure, method, restPath, input);
            }

            @Override
            public void addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties, RestEndpointListener listener) {

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

                            // Obtain the path and method
                            String pathMinusExtension = resourcePath.substring(0, resourcePath.length() - (".".length() + yamlExtension.length()));

                            // Split to method and path
                            int index = pathMinusExtension.lastIndexOf('.');
                            if (index > 0) {

                                // Obtain the method and path
                                String method = pathMinusExtension.substring(index + ".".length());
                                String path = pathMinusExtension.substring(0, index);

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
                                RestEndpoint endpoint = this.addRestService(endpointContext.isSecure(), endpointContext.getHttpMethod(), endpointContext.getPath(), classpathResourcePath, properties);

                                // Inform listener
                                if (listener != null) {
                                    listener.endpoint(endpoint);
                                }
                            }
                        }
                    }
                }
            }
        };
    }

}
