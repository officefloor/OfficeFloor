package net.officefloor.web.rest.build;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import net.officefloor.activity.compose.ComposeConfiguration;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeContext;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.web.build.WebArchitect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class RestEmployer {

    /**
     * Employs the {@link RestArchitect}.
     *
     * @param officeArchitect     {@link OfficeArchitect}.
     * @param webArchitect        {@link WebArchitect}.
     * @param composeArchitect    {@link ComposeArchitect}.
     * @param officeSourceContext {@link OfficeSourceContext}.
     * @return {@link RestArchitect}.
     */
    public static RestArchitect employRestArchitect(OfficeArchitect officeArchitect, WebArchitect webArchitect, ComposeArchitect composeArchitect, OfficeSourceContext officeSourceContext) {
        return new RestArchitectImpl(officeArchitect, webArchitect, composeArchitect, officeSourceContext);
    }

    /**
     * Determines if REST endpoints are configured.
     *
     * @param resourceDirectory Directory containing the REST configuration.
     * @return <code>true</code> if REST endpoint configuration available.
     * @throws Exception If fails to check for REST configuration files.
     */
    public static boolean isRestAvailable(String resourceDirectory) throws Exception {
        return ComposeEmployer.isCompositionsAvailable(resourceDirectory, RestEmployer::isRestCompositionFile);
    }

    protected static boolean isRestCompositionFile(String itemName) {
        int lastDot = itemName.lastIndexOf('.');
        return lastDot > 0;
    }

    protected static class RestArchitectImpl implements RestArchitect {

        private final OfficeArchitect officeArchitect;
        private final WebArchitect webArchitect;
        private final ComposeArchitect composeArchitect;
        private final OfficeSourceContext officeSourceContext;

        private final List<RestMethodDecorator<?>> decorators = new LinkedList<>();

        protected RestArchitectImpl(OfficeArchitect officeArchitect, WebArchitect webArchitect, ComposeArchitect composeArchitect, OfficeSourceContext officeSourceContext) {
            this.officeArchitect = officeArchitect;
            this.webArchitect = webArchitect;
            this.composeArchitect = composeArchitect;
            this.officeSourceContext = officeSourceContext;
        }

        /*
         * ================== RestArchitect =========================
         */

        @Override
        public boolean isRestAvailable(String resourceDirectory) throws Exception {
            return this.composeArchitect.isCompositionsAvailable(resourceDirectory, RestEmployer::isRestCompositionFile);
        }

        @Override
        public <M> MomentoKey<M> addRestMethodDecorator(RestMethodDecorator<M> decorator) {
            int decoratorIndex = this.decorators.size();
            this.decorators.add(decorator);
            return new MomentoKeyImpl<>(decoratorIndex);
        }

        @Override
        public RestEndpoint addRestService(boolean isSecure, HttpMethod method, String restPath, String compositionLocation, PropertyList properties, RestConfiguration endpointConfiguration) throws Exception {

            // Compose servicing
            ComposedEndpoint composedEndpoint = this.composeArchitect.addComposition("REST_" + method.getName() + "_" + restPath,
                    RestEmployer::createComposedEndpoint, compositionLocation, properties,
                    ComposeConfiguration.class);

            // Create root context
            final RestPathContextImpl root = new RestPathContextImpl("/", null);

            // Obtain the rest path context
            RestPathContextImpl restPathContext = root.getRestPathContext(restPath);
            restPathContext.addConfiguration(endpointConfiguration);

            // Add the REST method
            restPathContext.addRestMethod(new RestMethodContextImpl<>(isSecure, method, restPathContext,
                    composedEndpoint.input, composedEndpoint.configuration, this.officeArchitect, this.webArchitect,
                    this.composeArchitect, this.officeSourceContext));

            // Decorate the REST methods
            restPathContext.decorateRestMethods(this.decorators);

            // Create and return the REST endpoint
            return restPathContext.buildRestEndpoint(this.webArchitect, this.officeArchitect, this.officeSourceContext);
        }

        @Override
        public Map<String, RestEndpoint> addRestServices(boolean isSecure, String resourceDirectory, PropertyList properties) throws Exception {

            // Create root context
            final RestPathContextImpl root = new RestPathContextImpl("/", null);

            // Load the rest end points
            Map<String, RestPathContextImpl> restEndpoints = new HashMap<>();
            composeArchitect.addCompositions("REST", (composeContext, composeListener) -> {

                // Obtain the compose path
                String composePath = composeContext.getItemName();

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
                }

                // Handle root
                if ("index".equalsIgnoreCase(endpointPath)) {
                    endpointPath = "/";
                }

                // Include slash to begin path
                if (!endpointPath.startsWith("/")) {
                    endpointPath = "/" + endpointPath;
                }

                // Obtain the endpoint to load
                RestPathContextImpl endpointContext = root.getRestPathContext(endpointPath);

                // Load appropriate information
                if (endpointMethod == null) {

                    // Configuration to the general REST endpoint
                    RestEndpointConfig endpointConfig = composeContext.getConfiguration(RestEndpointConfig.class);
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
                                return ComposeEmployer.MAPPER.treeToValue(node, type);
                            } catch (Exception ex) {
                                officeArchitect.addIssue("Failed to obtain configuration item " + itemName + " from " + composePath + " (for type " + type.getName() + ")", ex);
                                return null;
                            }
                        }
                    });

                } else {

                    // Create the composition for handling the REST method
                    ComposedEndpoint composedEndpoint = composeContext.addComposition(
                            "REST_" + composePath,
                            RestEmployer::createComposedEndpoint, ComposeConfiguration.class);

                    // Create and initialise the context
                    RestMethodContextImpl<?> methodContext = new RestMethodContextImpl<>(
                            isSecure, endpointMethod, endpointContext,
                            composedEndpoint.input, composedEndpoint.configuration, this.officeArchitect,
                            this.webArchitect, this.composeArchitect, this.officeSourceContext);
                    endpointContext.addRestMethod(methodContext);
                }

            }, resourceDirectory, properties, (itemName, item) -> {
                // Need all files read before end point creation
            });

            // Decorate the REST methods
            root.decorateRestMethods(this.decorators);

            // Build the REST endpoints
            Map<String, RestEndpoint> endpoints = new HashMap<>();
            root.loadRestEndpoints(endpoints, this.webArchitect, this.officeArchitect, this.officeSourceContext);

            // Return the REST endpoints
            return endpoints;
        }
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
