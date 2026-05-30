package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.officefloor.activity.compose.build.ComposeArchitect;
import net.officefloor.activity.compose.build.ComposeEmployer;
import net.officefloor.activity.team.build.TeamEmployer;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.plugin.section.clazz.MethodParameterAnnotation;
import net.officefloor.spring.starter.rest.argument.SpringArgumentManagedObjectSource;
import net.officefloor.spring.starter.rest.argument.SpringBeanSupplierSource;
import net.officefloor.spring.starter.rest.argument.SpringMvcArguments;
import net.officefloor.spring.starter.rest.argument.SpringTypeQualifierInterrogator;
import net.officefloor.spring.starter.rest.cors.ComposeCorsConfiguration;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandler;
import net.officefloor.spring.starter.rest.response.SpringExceptionHandlerServiceFactory;
import net.officefloor.spring.starter.rest.response.SpringHttpObjectResponderFactory;
import net.officefloor.spring.starter.rest.servlet.HttpServletRequestManagedObjectSource;
import net.officefloor.spring.starter.rest.servlet.HttpServletResponseManagedObjectSource;
import net.officefloor.spring.starter.rest.view.ViewResponse;
import net.officefloor.spring.starter.rest.view.ViewResponseManagedObjectSource;
import net.officefloor.web.WebArchitectEmployer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.json.JacksonHttpObjectParserFactory;
import net.officefloor.web.openapi.OpenApiArchitect;
import net.officefloor.web.openapi.build.OpenApiEmployer;
import net.officefloor.web.rest.build.MomentoKey;
import net.officefloor.web.rest.build.RestArchitect;
import net.officefloor.web.rest.build.RestEmployer;
import net.officefloor.web.rest.build.RestEndpoint;
import net.officefloor.web.rest.build.RestMethod;
import net.officefloor.web.rest.build.RestMethodDecoratorContext;
import net.officefloor.web.rest.build.RestPathContext;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.cors.CorsConfiguration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SpringBootOfficeSource extends AbstractOfficeSource {

    private final ObjectMapper objectMapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints;

    private final ConfigurableApplicationContext applicationContext;

    private final Map<String, OfficeManagedObject> springArguments = new HashMap<>();

    private final OpenAPI openApi;

    public SpringBootOfficeSource(ObjectMapper objectMapper,
                                  List<OfficeFloorRestEndpoint> restEndpoints,
                                  ConfigurableApplicationContext applicationContext,
                                  OpenAPI openApi) {
        this.objectMapper = objectMapper;
        this.restEndpoints = restEndpoints;
        this.applicationContext = applicationContext;
        this.openApi = openApi;
    }

    /**
     * Loads the {@link CorsConfiguration} from the {@link RestPathContext}.
     *
     * @param path {@link RestPathContext}.
     * @return {@link CorsConfiguration} or <code>null</code> if no CORS configured.
     */
    private static CorsConfiguration loadCorsConfiguration(RestPathContext path) {

        // Drop out of recursion when at root
        if (path == null) {
            return null;
        }

        // Shorter path has lower precedence
        CorsConfiguration parentCors = loadCorsConfiguration(path.getParentPath());

        // Obtain the CORS from current path segment
        ComposeCorsConfiguration composeConfiguration = path.getConfiguration("cors", ComposeCorsConfiguration.class);

        // Return the combined CORS, taking longer path as precedence
        return OfficeFloorRestEndpoint.combineCors(OfficeFloorRestEndpoint.createCorsConfiguration(composeConfiguration), parentCors);
    }

    /*
     * ======================= OfficeSource ========================
     */

    @Override
    protected void loadSpecification(SpecificationContext specificationContext) {
        // No specification
    }

    @Override
    public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext officeSourceContext) throws Exception {

        // Employ the architects
        WebArchitect webArchitect = WebArchitectEmployer.employWebArchitect(officeArchitect, officeSourceContext);
        ComposeArchitect composeArchitect = ComposeEmployer.employComposeArchitect(officeArchitect, officeSourceContext);
        RestArchitect restArchitect = RestEmployer.employRestArchitect(officeArchitect, webArchitect, composeArchitect, officeSourceContext);

        // Enable auto-wiring of teams to managed functions, but only when teams are configured
        TeamEmployer.enableOfficeAutoWireTeams("officefloor/teams", officeArchitect);

        // Undertake Spring Boot extensions
        for (OfficeFloorSpringBootExtension springBootExtension : officeSourceContext.loadOptionalServices(OfficeFloorSpringBootExtensionServiceFactory.class)) {
            springBootExtension.extendSpringBootSupport(new OfficeFloorSpringBootExtensionContext() {
                @Override
                public OfficeArchitect getOfficeArchitect() {
                    return officeArchitect;
                }

                @Override
                public OfficeSourceContext getOfficeSourceContext() {
                    return officeSourceContext;
                }

                @Override
                public ComposeArchitect getComposeArchitect() {
                    return composeArchitect;
                }

                @Override
                public RestArchitect getRestArchitect() {
                    return restArchitect;
                }

                @Override
                public ConfigurableApplicationContext getApplicationContext() {
                    return SpringBootOfficeSource.this.applicationContext;
                }
            });
        }

        // Configure object requests
        webArchitect.addHttpObjectParser(new JacksonHttpObjectParserFactory(this.objectMapper));

        // Load the Spring Exception Handlers
        List<SpringExceptionHandler> springExceptionHandlersList = new LinkedList<>();
        for (SpringExceptionHandler springExceptionHandler : officeSourceContext.loadOptionalServices(SpringExceptionHandlerServiceFactory.class)) {
            if (springExceptionHandler != null) {
                springExceptionHandlersList.add(springExceptionHandler);
            }
        }
        SpringExceptionHandler[] springExceptionHandlers = springExceptionHandlersList.toArray(SpringExceptionHandler[]::new);

        // Allow Spring to handle responses
        webArchitect.addHttpObjectResponder(new SpringHttpObjectResponderFactory(springExceptionHandlers));

        // Add CORS decoration
        MomentoKey<CorsConfiguration> corsMomento = restArchitect.addRestMethodDecorator((context) -> {
            CorsConfiguration pathCorsConfiguration = loadCorsConfiguration(context.getPath());
            CorsConfiguration methodCorsConfiguration = OfficeFloorRestEndpoint.createCorsConfiguration(context.getConfiguration("cors", ComposeCorsConfiguration.class));
            context.setMomento(OfficeFloorRestEndpoint.combineCors(pathCorsConfiguration, methodCorsConfiguration));
        });

        // Add the rest servicing
        officeSourceContext.getLogger().info("Loading REST endpoints:");
        PropertyList propertyList = officeSourceContext.createPropertyList();
        for (String propertyName : officeSourceContext.getPropertyNames()) {
            propertyList.addProperty(propertyName).setValue(officeSourceContext.getProperty(propertyName));
        }
        Map<String, RestEndpoint> restEndpoints = restArchitect.addRestServices(false, "officefloor/rest", propertyList);

        // Include the REST endpoints (sorted for deterministic loading and logging)
        List<String> endpointPaths = restEndpoints.keySet().stream().sorted((a, b) -> {
            int longerSecond = a.length() - b.length(); // short path first
            return (longerSecond != 0) ? longerSecond : String.CASE_INSENSITIVE_ORDER.compare(a, b);
        }).toList();
        for (String endpointPath : endpointPaths) {
            RestEndpoint restEndpoint = restEndpoints.get(endpointPath);

            // Include the rest endpoint
            this.restEndpoints.add(new OfficeFloorRestEndpoint(restEndpoint, corsMomento));

            // Log the REST methods (in deterministic order)
            List<RestMethod> sortedRestMethods = restEndpoint.getRestMethods().stream()
                    .sorted((a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getHttpMethod().getName(), b.getHttpMethod().getName()))
                    .toList();
            for (RestMethod restMethod : sortedRestMethods) {
                officeSourceContext.getLogger().info("  " + restMethod.getHttpMethod().getName() + " " + restEndpoint.getPath());
            }
        }

        // Register all the Spring components for use
        ConfigurableListableBeanFactory beanFactory = this.applicationContext.getBeanFactory();
        officeArchitect.addSupplier("SPRING", new SpringBeanSupplierSource(beanFactory));

        // Add access to servlet request/response
        this.addOfficeManagedObjectSource(HttpServletRequest.class, new HttpServletRequestManagedObjectSource(), officeArchitect);
        this.addOfficeManagedObjectSource(HttpServletResponse.class, new HttpServletResponseManagedObjectSource(), officeArchitect);
        webArchitect.enableHttpExternalResponse();

        // Allow rendering of views
        this.addOfficeManagedObjectSource(ViewResponse.class, new ViewResponseManagedObjectSource(), officeArchitect);

        // Obtain the Spring argument checker
        SpringMvcArguments.SpringArgumentChecker springArgumentChecker = SpringMvcArguments.getSpringArgumentChecker(officeSourceContext);

        // Load in-line configured spring dependencies
        officeArchitect.addManagedFunctionAugmentor((context) -> {
            ManagedFunctionType<?, ?> functionType = context.getManagedFunctionType();
            for (ManagedFunctionObjectType<?> functionParameterType : functionType.getObjectTypes()) {
                Class<?> objectType = functionParameterType.getObjectType();

                // Obtain the method parameter annotation
                MethodParameterAnnotation parameterAnnotation = functionParameterType.getAnnotation(MethodParameterAnnotation.class);
                if (parameterAnnotation != null) {

                    // Obtain the method details for the parameter
                    Method method = parameterAnnotation.getMethod();
                    int parameterIndex = parameterAnnotation.getParameterIndex();

                    // Determine if in-line configuration of dependency
                    if (springArgumentChecker.isSpringArgument(objectType, functionParameterType.getAnnotations())) {
                        this.handleSpringArgument(objectType, method, parameterIndex, officeArchitect);
                    }
                }
            }
        });

        // Configure the Open API
        OpenApiArchitect openApiArchitect = OpenApiEmployer.employOpenApiArchitect(officeArchitect, webArchitect, null, officeSourceContext);
        openApiArchitect.buildOpenApi(this.openApi, new HashSet<>());

        // Configure Office
        webArchitect.informOfficeArchitect();
    }

    private <S> void addOfficeManagedObjectSource(Class<S> objectType, ManagedObjectSource<?, ?> managedObjectSource, OfficeArchitect officeArchitect) {
        officeArchitect.addOfficeManagedObjectSource(objectType.getSimpleName(), managedObjectSource)
                .addOfficeManagedObject(objectType.getSimpleName(), ManagedObjectScope.THREAD);
    }

    private void handleSpringArgument(Class<?> objectType, Method method, int parameterIndex, OfficeArchitect officeArchitect) {

        // Determine the binding name and qualifier
        String bindAndQualifier = SpringTypeQualifierInterrogator.getSpringTypeQualifier(method, parameterIndex);

        // Set up the spring argument managed object
        this.springArguments.computeIfAbsent(bindAndQualifier, (key) -> {

            // Add the argument object
            OfficeManagedObject argumentObject = officeArchitect.addOfficeManagedObjectSource(bindAndQualifier, new SpringArgumentManagedObjectSource(objectType, method, parameterIndex))
                    .addOfficeManagedObject(bindAndQualifier, ManagedObjectScope.THREAD);

            // Add qualifier
            argumentObject.addTypeQualification(bindAndQualifier, objectType.getName());

            // Return the argument object
            return argumentObject;
        });
    }

}
