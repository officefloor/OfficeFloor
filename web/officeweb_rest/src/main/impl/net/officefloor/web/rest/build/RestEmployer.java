package net.officefloor.web.rest.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.WebArchitect;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestEmployer {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

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
                                               String compositionLocation, PropertyList properties,
                                               RestConfiguration configuration) throws Exception {

                // Compose servicing
                ComposedEndpoint composedEndpoint = composeArchitect.addComposition("REST_" + method.getName() + "_" + restPath,
                        RestEmployer::createComposedEndpoint, compositionLocation, properties,
                        ComposeConfiguration.class);

                // Create the context
                RestEndpointContextImpl context = new RestEndpointContextImpl(isSecure, restPath);
                context.addConfiguration(configuration);
                context.addRestMethod(new RestMethodContextImpl(isSecure, method, restPath,
                        composedEndpoint.input, composedEndpoint.configuration));

                // Create and return the REST endpoint
                return context.buildRestEndpoint(webArchitect, officeArchitect);
            }

            @Override
            public void addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties,
                                        RestListener listener) throws Exception {

                // Load the rest end points
                Map<String, RestEndpointContextImpl> restEndpoints = new HashMap<>();
                composeArchitect.addCompositions((context, composeListener) -> {

                    // Obtain the compose path
                    String composePath = context.getItemName();

                    // Determine end point path and method
                    String endpointPath;
                    HttpMethod endpointMethod;
                    int index = composePath.lastIndexOf('.');
                    if (index <= 0) {
                        // No method
                        endpointPath = composePath;
                        endpointMethod = null;

                    } else {
                        // Obtain the method and path
                        endpointPath = composePath.substring(0, index);
                        endpointMethod = HttpMethod.getHttpMethod(composePath.substring(index + ".".length()).toUpperCase());

                        // Handle root
                        if ("index".equalsIgnoreCase(endpointPath)) {
                            endpointPath = "/";
                        }
                    }

                    // Obtain the endpoint to load
                    final String finalEndpointPath = endpointPath;
                    RestEndpointContextImpl endpointContext = restEndpoints.computeIfAbsent(endpointPath,
                            (key) -> {
                                RestEndpointContextImpl impl = new RestEndpointContextImpl(isSecure, finalEndpointPath);
                                if (listener != null) {
                                    listener.initialiseRestEndpoint(impl);
                                }
                                return impl;
                            });

                    // Load appropriate information
                    if (endpointMethod == null) {

                        // Configuration to the general REST endpoint
                        RestEndpointConfig endpointConfig = context.getConfiguration(RestEndpointConfig.class);
                        endpointContext.addConfiguration(new RestConfiguration() {
                            @Override
                            public <T> T getConfiguration(String itemName, Class<T> type) {

                                // Obtain the node for the item
                                JsonNode node = endpointConfig.getItems().get(itemName);
                                if (node == null) {
                                    return null; // nothing configured
                                }

                                // Translate to configuration type
                                try {
                                    return MAPPER.treeToValue(node, type);
                                } catch (Exception ex) {
                                    officeArchitect.addIssue("Failed to obtain configuration item " + itemName + " from " + composePath + " (for type " + type.getName() + ")", ex);
                                    return null;
                                }
                            }
                        });

                    } else {

                        // Create the composition for handling the REST method
                        ComposedEndpoint composedEndpoint = context.addComposition(
                                "REST_" + composePath,
                                RestEmployer::createComposedEndpoint, ComposeConfiguration.class);

                        // Create and initialise the context
                        RestMethodContextImpl methodContext = new RestMethodContextImpl(
                                endpointContext.isSecure(), endpointMethod, endpointPath,
                                composedEndpoint.input, composedEndpoint.configuration);
                        listener.initialiseRestMethod(methodContext);
                        endpointContext.addRestMethod(methodContext);
                    }

                }, resourceDirectory, properties, (itemName, item) -> {
                    // Need all files read before end point creation
                });

                // Send the REST endpoints (in order)
                List<String> sortedPaths = restEndpoints.keySet().stream().sorted().toList();
                for (String path : sortedPaths) {

                    // Build the REST endpoint
                    RestEndpointContextImpl endpointContext = restEndpoints.get(path);
                    RestEndpoint restEndpoint = endpointContext.buildRestEndpoint(webArchitect, officeArchitect);

                    // Notify of the REST endpoint
                    listener.endpoint(restEndpoint);
                }
            }
        };
    }

    protected static class ComposedEndpoint {
        private final OfficeSectionInput input;
        private final RestConfiguration configuration;

        public ComposedEndpoint(OfficeSectionInput input, RestConfiguration configuration) {
            this.input = input;
            this.configuration = configuration;
        }
    }

    protected static ComposedEndpoint createComposedEndpoint(ComposeContext<?> context) {
        return new ComposedEndpoint(context.getStartFunction(), new RestConfiguration() {

            @Override
            public <T> T getConfiguration(String itemName, Class<T> type) {
                return context.getConfiguration(itemName, type);
            }
        });
    }


}
